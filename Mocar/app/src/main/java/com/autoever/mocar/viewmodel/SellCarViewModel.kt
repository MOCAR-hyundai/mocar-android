package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoever.mocar.data.listings.ListingDto
import com.autoever.mocar.data.listings.toCar
import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.ui.common.util.sanitize
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun nowKstText(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
    fmt.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    return fmt.format(Date())
}


/* ---- status 정의 ---- */
object ListingStatus {
    const val ON_SALE  = "on_sale"
    const val RESERVED = "reserved"
    const val SOLD     = "sold"
    const val DRAFT    = "draft"     // 등록 전 혹은 비활성
}

/* ---------- UI States ---------- */

data class ListingLookupState(
    val loading: Boolean = false,
    val car: Car? = null,                // CarDetailStep에서 표시할 도메인 모델
    val raw: ListingDto? = null,         // 저장 시 쓸 원본 DTO
    val error: String? = null
)

data class SellSubmitState(
    val loading: Boolean = false,
    val done: Boolean = false,
    val error: String? = null
)

/* ---------- ViewModel ---------- */

class SellCarViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val listings = db.collection("listings")

    private val _lookup = MutableStateFlow(ListingLookupState())
    val lookup: StateFlow<ListingLookupState> = _lookup

    private val _submit = MutableStateFlow(SellSubmitState())
    val submit: StateFlow<SellSubmitState> = _submit

    /** 차량번호 + 소유자명으로 listings에서 1건 검색 */
    fun findListing(plateNo: String, ownerName: String) {
        val plate = plateNo.sanitize()
        val owner = ownerName.sanitize()

        if (plate.isBlank() || owner.isBlank()) {
            _lookup.value = ListingLookupState(error = "번호판과 소유자명을 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _lookup.value = ListingLookupState(loading = true)
            try {
                val snap = listings
                    .whereEqualTo("plateNo", plate)
                    .whereEqualTo("ownerName", owner)
                    .limit(1)
                    .get()
                    .await()

                val dto = snap.documents.firstOrNull()?.toObject(ListingDto::class.java)
                    ?.copy(listingId = snap.documents.first().id) // 문서 id 보정

                _lookup.value = if (dto != null) {
                    ListingLookupState(
                        loading = false,
                        car = dto.toCar(),
                        raw = dto
                    )
                } else {
                    ListingLookupState(loading = false, error = "해당 차량을 찾을 수 없어요.")
                }
            } catch (e: Exception) {
                _lookup.value = ListingLookupState(
                    loading = false, error = e.message ?: "검색 중 오류가 발생했어요."
                )
            }
        }
    }

    /**
     * 판매 정보 저장:
     * - status 가 "on_sale" 이면 막기
     * - 아니라면 mileage/price/description/images/sellerId/status 업데이트
     */
    fun submitSale(
        mileageKm: Long?,
        hopePrice: Long?,
        description: String?,
        images: List<String>
    ) {
        val dto = _lookup.value.raw ?: run {
            _submit.value = SellSubmitState(error = "먼저 차량을 조회해주세요.")
            return
        }

        viewModelScope.launch {
            _submit.value = SellSubmitState(loading = true)
            try {
                val ref = listings.document(dto.listingId)
                val current = ref.get().await()
                if (!current.exists()) {
                    _submit.value = SellSubmitState(loading = false, error = "매물을 찾을 수 없어요.")
                    return@launch
                }

                val currentStatus = current.getString("status") ?: ListingStatus.DRAFT
                if (currentStatus == ListingStatus.ON_SALE || currentStatus == ListingStatus.RESERVED) {
                    _submit.value = SellSubmitState(
                        loading = false,
                        error = when (currentStatus) {
                            ListingStatus.ON_SALE  -> "이미 판매중인 매물이에요."
                            ListingStatus.RESERVED -> "예약중인 매물이라 등록할 수 없어요."
                            else -> "이미 판매 진행 중인 매물이에요."
                        }
                    )
                    return@launch
                }

                val updates = mutableMapOf<String, Any>(
                    "status" to "on_sale",
                    "updatedAt" to nowKstText()                   //문자열로 저장
                )
                mileageKm?.let { updates["mileage"] = it }
                hopePrice?.let { updates["price"] = it }
                description?.takeIf { it.isNotBlank() }?.let { updates["description"] = it }
                if (images.isNotEmpty()) updates["images"] = images

                auth.currentUser?.uid?.takeIf { it.isNotBlank() }?.let { updates["sellerId"] = it }

                ref.update(updates).await()

                // 성공 후 최신값 재조회하여 UI 갱신
                val refreshedDoc = ref.get().await()
                val refreshed = refreshedDoc.toObject(ListingDto::class.java)
                    ?.copy(listingId = dto.listingId)

                if (refreshed != null) {
                    _lookup.value = ListingLookupState(
                        loading = false,
                        car = refreshed.toCar(),
                        raw = refreshed
                    )
                    _submit.value = SellSubmitState(loading = false, done = true)
                } else {
                    _submit.value = SellSubmitState(
                        loading = false,
                        error = "등록은 되었지만 데이터를 불러오지 못했어요."
                    )
                }
            } catch (e: Exception) {
                _submit.value = SellSubmitState(
                    loading = false,
                    error = e.message ?: "등록 중 오류가 발생했어요."
                )
            }
        }
    }
    fun clearStates() {
        _lookup.value = ListingLookupState()
        _submit.value = SellSubmitState()
    }
}