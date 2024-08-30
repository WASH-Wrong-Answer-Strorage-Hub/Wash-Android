import com.wash.washandroid.presentation.fragment.home.ApiResponse
import com.wash.washandroid.presentation.fragment.home.DeleteProblemResponse
import com.wash.washandroid.presentation.fragment.home.EditFolder
import com.wash.washandroid.presentation.fragment.home.ProblemSearchResponse
import com.wash.washandroid.presentation.fragment.home.ProblemsResponse
import com.wash.washandroid.presentation.fragment.home.ReorderRequest
import com.wash.washandroid.presentation.fragment.home.ReorderResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    //전체 폴더 조회
    @GET("folders")
    fun getFolders(
        @Header("Authorization") accessToken: String
    ): Call<ApiResponse>

    //폴더 이름 수정
    @PATCH("folders/{folderId}")
    fun updateFolderName(
        @Header("Authorization") token: String,
        @Path("folderId") folderId: Int,
        @Body requestBody: Map<String, String>
    ): Call<EditFolder>

    //폴더 삭제
    @DELETE("folders/{folderId}")
    fun deleteFolder(
        @Header("Authorization") token: String,
        @Path("folderId") folderId: Int
    ): Call<ApiResponse>

    //파일 속 문제 미리보기
    @GET("folders/{folderId}/problems")
    fun getImagesForFolder(
        @Header("Authorization") authHeader: String,
        @Path("folderId") folderId: Int
    ): Call<ProblemsResponse>


    @DELETE("problems/{problemId}")
    fun deleteProblem(
        @Header("Authorization") token: String,
        @Path("problemId") problemId: Int
    ): Call<DeleteProblemResponse>

    // 문제 검색 (특정 폴더 또는 전체 문제)
    /*    folderId == null : 전체 /    folderId != null : 특정    */
    @GET("problems/search")
    fun searchProblems(
        @Header("Authorization") authHeader: String,
        @Query("folderId") folderId: Int? = null, // folderId가 null일 수 있도록 수정
        @Query("query") query: String
    ): Call<ProblemSearchResponse>

    // 폴더 순서 조정
    @PATCH("folders/order")
    fun reorderFolders(
        @Header("Authorization") token: String,
        @Body requestBody: ReorderRequest
    ): Call<ReorderResponse>


}