package com.autoever.mocar.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autoever.mocar.data.favorites.FavoriteDto
import com.autoever.mocar.data.listings.ListingDto
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class SearchFilterState(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..100000f,
    val yearRange: ClosedFloatingPointRange<Float> = 1990f..2025f,
    val mileageRange: ClosedFloatingPointRange<Float> = 0f..300000f,
    val selectedTypes: List<String> = emptyList(),
    val selectedFuels: List<String> = emptyList(),
    val selectedRegions: List<String> = emptyList()
)

// 화면 로딩
data class ListingUiState(
    val isLoading: Boolean = true,
    val listings: List<ListingDto> = emptyList()
)

// 검색기록
data class SearchRecordItem(
    val docId: String,
    val brand: String?,
    val model: String?,
    val subModels: List<String>,
    val priceRange: ClosedFloatingPointRange<Float>,
    val yearRange: ClosedFloatingPointRange<Float>,
    val mileageRange: ClosedFloatingPointRange<Float>,
    val selectedTypes: List<String>,
    val selectedFuels: List<String>,
    val selectedRegions: List<String>
)

// 필터 > 상세정보
data class FilterParams(
    val brand: String?,
    val model: String?,
    val subModels: List<String>,
    val minPrice: Float,
    val maxPrice: Float,
    val minYear: Float,
    val maxYear: Float,
    val minMileage: Float,
    val maxMileage: Float,
    val types: List<String>,
    val fuels: List<String>,
    val regions: List<String>
)

// 필터 > 검색 결과 필터
data class ResultFilterParams(
    val subModels: List<String>,
    val minPrice: Float,
    val maxPrice: Float,
    val minYear: Float,
    val maxYear: Float,
    val minMileage: Float,
    val maxMileage: Float,
    val types: List<String>,
    val fuels: List<String>,
    val regions: List<String>
)

class SearchManufacturerViewModel : ViewModel() {
    var isTransitionLoading by mutableStateOf(false)
    var selectedBrand by mutableStateOf<String?>(null)
    var selectedModel by mutableStateOf<String?>(null)
    var selectedSubModels = mutableStateListOf<String>()

    fun clearAll() {
        selectedBrand = null
        selectedModel = null
        selectedSubModels.clear()
    }
    fun restoreFrom(item: SearchRecordItem) {
        selectedBrand = item.brand
        selectedModel = item.model
        selectedSubModels.clear()
        selectedSubModels.addAll(item.subModels)
    }

}

class SearchFilterViewModel(application: Application) : AndroidViewModel(application) {
    // 필터링 상태
    private val _filterState = MutableStateFlow(SearchFilterState())
    val filterState: StateFlow<SearchFilterState> = _filterState

    // 필터 기록
    private val _filterHistory = MutableStateFlow<List<SearchRecordItem>>(emptyList())
    val filterHistory: StateFlow<List<SearchRecordItem>> = _filterHistory


    // 필터 후 상세 화면용
    var filterParams by mutableStateOf<FilterParams?>(null)
        private set

    private val db = Firebase.firestore

    fun setFilterParamsFromCurrentState(
        brand: String?,
        model: String?,
        subModels: List<String>,
        filterState: SearchFilterState
    ) {
        filterParams = FilterParams(
            brand = brand,
            model = model,
            subModels = subModels,
            minPrice = filterState.priceRange.start,
            maxPrice = filterState.priceRange.endInclusive,
            minYear = filterState.yearRange.start,
            maxYear = filterState.yearRange.endInclusive,
            minMileage = filterState.mileageRange.start,
            maxMileage = filterState.mileageRange.endInclusive,
            types = filterState.selectedTypes,
            fuels = filterState.selectedFuels,
            regions = filterState.selectedRegions
        )
        println(filterParams)
    }

    // 가격, 연식, 주행거리 업데이트
    fun updatePrice(range: ClosedFloatingPointRange<Float>) {
        _filterState.update { it.copy(priceRange = range) }
    }

    fun updateYear(range: ClosedFloatingPointRange<Float>) {
        _filterState.update { it.copy(yearRange = range) }
    }

    fun updateMileage(range: ClosedFloatingPointRange<Float>) {
        _filterState.update { it.copy(mileageRange = range) }
    }

    // 체크박스 목록들 업데이트
    fun toggleType(type: String) {
        _filterState.update {
            val updated = if (type in it.selectedTypes) it.selectedTypes - type else it.selectedTypes + type
            it.copy(selectedTypes = updated)
        }
    }

    fun toggleFuel(fuel: String) {
        _filterState.update {
            val updated = if (fuel in it.selectedFuels) it.selectedFuels - fuel else it.selectedFuels + fuel
            it.copy(selectedFuels = updated)
        }
    }

    fun toggleRegion(region: String) {
        _filterState.update {
            val updated = if (region in it.selectedRegions) it.selectedRegions - region else it.selectedRegions + region
            it.copy(selectedRegions = updated)
        }
    }
    // 초기화
    fun clearAll() {
        _filterState.value = SearchFilterState()
    }

    // 파이어베이스에 필터 데이터 저장 (최근 검색기록)
    fun saveSearchHistory(
        userId: String,
        brand: String?,
        model: String?,
        subModels: List<String>,
        filterState: SearchFilterState
    ) {
        // 필터 데이터를 Map으로 구성 (timestamp는 아래에서 제외)
        val filterData = mapOf(
            "userId" to userId,
            "brand" to brand,
            "model" to model,
            "subModels" to subModels.sorted(),
            "minPrice" to filterState.priceRange.start.toInt(),
            "maxPrice" to filterState.priceRange.endInclusive.toInt(),
            "minYear" to filterState.yearRange.start.toInt(),
            "maxYear" to filterState.yearRange.endInclusive.toInt(),
            "minMileage" to filterState.mileageRange.start.toInt(),
            "maxMileage" to filterState.mileageRange.endInclusive.toInt(),
            "carTypes" to filterState.selectedTypes.sorted(),
            "fuels" to filterState.selectedFuels.sorted(),
            "regions" to filterState.selectedRegions.sorted(),
            "timestamp" to FieldValue.serverTimestamp()
        )

        // 🔸 중복 체크용 signature 생성 (timestamp, userId 제외)
        val currentSignature = filterData
            .filterKeys { it != "userId" && it != "timestamp" }
            .mapValues { entry ->
                when (val value = entry.value) {
                    is List<*> -> value.map { it.toString() }.sorted()
                    is Number -> value.toString()
                    else -> value?.toString() ?: ""
                }
            }
            .toSortedMap()
            .toString()

        db.collection("recent_filter")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val isDuplicate = result.any { doc ->
                    val docSignature = doc.data
                        .filterKeys { it != "userId" && it != "timestamp" }
                        .mapValues { entry ->
                            when (val value = entry.value) {
                                is List<*> -> value.map { it.toString() }.sorted()
                                is Number -> value.toString()
                                else -> value?.toString() ?: ""
                            }
                        }
                        .toSortedMap()
                        .toString()

                    docSignature == currentSignature
                }

                if (!isDuplicate) {
                    db.collection("recent_filter")
                        .add(filterData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "필터 저장 성공")
                        }
                        .addOnFailureListener {
                            Log.e("Firestore", "필터 저장 실패", it)
                        }
                } else {
                    Log.d("Firestore", "⚠중복된 필터 - 저장 생략")
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "중복 필터 조회 실패", it)
            }
    }


    // 파이어베이스에 저장된 데이터 불러오기 (최근 검색기록)
    fun loadSearchHistory(userId: String) {

        db.collection("recent_filter")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val sortedDocs = result.sortedByDescending {
                    it.getTimestamp("timestamp")?.toDate()
                }
                val items = sortedDocs.mapNotNull { doc ->
                    try {
                        SearchRecordItem(
                            docId = doc.id,
                            brand = doc.getString("brand"),
                            model = doc.getString("model"),
                            subModels = doc.get("subModels") as? List<String> ?: emptyList(),
                            priceRange = doc.getLong("minPrice")!!.toFloat()..doc.getLong("maxPrice")!!.toFloat(),
                            yearRange = doc.getLong("minYear")!!.toFloat()..doc.getLong("maxYear")!!.toFloat(),
                            mileageRange = doc.getLong("minMileage")!!.toFloat()..doc.getLong("maxMileage")!!.toFloat(),
                            selectedTypes = doc.get("carTypes") as? List<String> ?: emptyList(),
                            selectedFuels = doc.get("fuels") as? List<String> ?: emptyList(),
                            selectedRegions = doc.get("regions") as? List<String> ?: emptyList()
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "파싱 실패: ${doc.id}", e)
                        null
                    }
                }

                _filterHistory.value = items
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "필터 기록 불러오기 실패", e)
            }
    }

    fun deleteEachHistory(item: SearchRecordItem) {

        val docId = item.docId

        if (docId.isNotBlank()) {
            db.collection("recent_filter").document(docId)
                .delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "필터 기록 삭제 성공")

                    // ViewModel 내 로컬 상태도 갱신
                    val current = _filterHistory.value.toMutableList()
                    current.remove(item)
                    _filterHistory.value = current
                }
                .addOnFailureListener {
                    Log.e("Firestore", "필터 기록 삭제 실패", it)
                }
        }
    }

    fun clearAllHistory(userId: String) {

        db.collection("recent_filter")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                // 파이어스토어에서 모든 해당 문서 삭제
                result.forEach { doc ->
                    db.collection("recent_filter").document(doc.id).delete()
                }

                // 로컬 상태도 초기화
                _filterHistory.value = emptyList()

                Log.d("Firestore", "최근 필터 검색 기록 전체 삭제 완료")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "최근 필터 기록 전체 삭제 실패", e)
            }
    }


    fun restoreFrom(item: SearchRecordItem) {
        _filterState.value = SearchFilterState(
            priceRange = item.priceRange,
            yearRange = item.yearRange,
            mileageRange = item.mileageRange,
            selectedTypes = item.selectedTypes,
            selectedFuels = item.selectedFuels,
            selectedRegions = item.selectedRegions
        )
    }
}

class ListingViewModel : ViewModel() {
    private var hasFetched = false
    private val db = Firebase.firestore
    private val _listings = MutableStateFlow<List<ListingDto>>(emptyList())
    val listings: StateFlow<List<ListingDto>> = _listings

    private val _uiState = MutableStateFlow(ListingUiState())
    val uiState: StateFlow<ListingUiState> = _uiState

    init {
        fetchListings()
    }

    private fun fetchListings() {
        if (hasFetched) return
        hasFetched = true

        _uiState.update { it.copy(isLoading = true) }
        db.collection("listings").get()
            .addOnSuccessListener { result ->

                val parsed = result.mapNotNull { doc ->
                    val brand = doc.getString("brand")
                    val model = doc.getString("model")
                    val fuel = doc.getString("fuel")
                    val carType = doc.getString("carType")
                    val region = doc.getString("region")
                    val imageList = doc.get("images") as? List<String> ?: emptyList()
                    val title = doc.getString("title") ?: ""
                    val year = doc.getLong("year")?.toInt() ?: 0

                    if (brand == null || model == null || fuel == null || region == null) {
                        Log.w("Firestore", "⚠️ Skipped doc ${doc.id} due to null required fields")
                        return@mapNotNull null
                    }

                    try {
                        doc.toObject(ListingDto::class.java)
                    } catch (e: Exception) {
                        Log.w("Firestore", "⚠️ Failed to parse doc ${doc.id}", e)
                        null
                    }
                }

                _listings.value = parsed
                _uiState.value = ListingUiState(
                    isLoading = false,
                    listings = parsed
                )

                Log.d("Firestore", "Parsed listings: ${parsed.size}")
                _listings.value = parsed
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch listings", e)
            }
    }
}

class SearchFilterViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchFilterViewModel::class.java)) {
            return SearchFilterViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// FavoriteDto 정의
//data class FavoriteDto(
//    val userId: String,
//    val listingId: String,
//    val createdAt: String
//)

// 검색 결과 viewmodel
class SearchResultViewModel : ViewModel() {
    private val _results = MutableStateFlow<List<ListingDto>>(emptyList())
    val results: StateFlow<List<ListingDto>> = _results

    // 찜 리스트 관리
    private val _favorites = MutableStateFlow<List<FavoriteDto>>(emptyList())
    val favorites: StateFlow<List<FavoriteDto>> = _favorites

    // Firebase
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val collectionName = "favorites"

    // 리스너 관리
    private var favListener: ListenerRegistration? = null
    private val authListener = FirebaseAuth.AuthStateListener { fa ->
        // 로그인 상태가 바뀌면 구독도 갱신
        startOrRestartFavoritesListener(fa.currentUser?.uid)
    }

    init {
        // 앱 시작 시 현재 유저로 구독 시작
        startOrRestartFavoritesListener(auth.currentUser?.uid)
        // 추후 로그인/로그아웃 대응
        auth.addAuthStateListener(authListener)
    }


    // 검색 결과 저장
    fun setResults(listings: List<ListingDto>) {
        _results.value = listings
    }

    // 선택적 초기화
    fun clearResults() {
        _results.value = emptyList()
    }

    fun addFavorite(listing: ListingDto) {
        val uid = auth.currentUser?.uid ?: return
        val fid = "${uid}_${listing.listingId}"

        val dto = FavoriteDto(
            fid = fid,
            userId = uid,
            listingId = listing.listingId
        )

        // 낙관적 UI 업데이트 (리스너로 곧 동기화됨)
        if (_favorites.value.none { it.fid == fid }) {
            _favorites.value = _favorites.value + dto
        }

        db.collection(collectionName)
            .document(fid)
            .set(dto)
            .addOnFailureListener {
                // 실패 시 롤백
                _favorites.value = _favorites.value.filterNot { it.fid == fid }
            }
    }

    /** 즐겨찾기 제거 */
    fun removeFavorite(listingId: String) {
        val uid = auth.currentUser?.uid ?: return
        val fid = "${uid}_${listingId}"

        // 낙관적 UI 업데이트
        _favorites.value = _favorites.value.filterNot { it.fid == fid }

        db.collection(collectionName)
            .document(fid)
            .delete()
            .addOnFailureListener {
                // 실패 시 되돌릴 수 있으면 복구(옵션)
                val restored = FavoriteDto(fid, uid, listingId)
                _favorites.value = (_favorites.value + restored).distinctBy { it.fid }
            }
    }
    /** 현재 사용자 uid로 favorites 실시간 구독 시작/재시작 */
    private fun startOrRestartFavoritesListener(uid: String?) {
        favListener?.remove()
        favListener = null

        if (uid == null) {
            _favorites.value = emptyList()
            return
        }

        favListener = db.collection(collectionName)
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener

                val list = snap?.documents?.mapNotNull { d ->
                    // createdAt이 문자열/타임스탬프 혼재해도 문자열로 통일
//                    val createdAtStr =
//                        d.getString("createdAt")
//                            ?: d.getTimestamp("createdAt")?.toDate()?.let {
//                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
//                                    .apply { timeZone = TimeZone.getTimeZone("Asia/Seoul") }
//                                    .format(it)
//                            }
//                            ?: ""

                    FavoriteDto(
                        fid = d.id,
                        userId = d.getString("userId") ?: return@mapNotNull null,
                        listingId = d.getString("listingId") ?: return@mapNotNull null
                    )
                }.orEmpty()

                _favorites.value = list
            }
    }

    override fun onCleared() {
        super.onCleared()
        favListener?.remove()
        auth.removeAuthStateListener(authListener)
    }
}