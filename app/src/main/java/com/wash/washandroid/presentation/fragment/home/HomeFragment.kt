import HomeViewModel
import MypageViewModel
import Note
import NoteAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.navercorp.nid.oauth.NidOAuthPreferencesManager.refreshToken
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentHomeBinding
import com.wash.washandroid.presentation.base.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false

    private lateinit var adapter: NoteAdapter
    private val homeViewModel: HomeViewModel by activityViewModels()

    // 토큰 받아오기
    private val mypageViewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        (activity as MainActivity).hideBottomNavigation(false)


        val refreshToken = mypageViewModel.getRefreshToken()
        val bearerToken = "Bearer $refreshToken"
        Log.d("homeFragment", "$refreshToken")
        Log.d("homeFragment", "$bearerToken")
        // fetchFolders에 token을 전달
        if (refreshToken != null) {
            homeViewModel.fetchFolders(refreshToken)
        }
        setupRecyclerView()
        observeViewModel()

        homeViewModel.images.observe(viewLifecycleOwner, { images ->
            // _images가 업데이트되었을 때 처리
            Log.d("HomeFragment", "Images updated: $images")
        })

        binding.editButton.setOnClickListener {
            isEditing = !isEditing
            adapter.setEditing(isEditing)
            binding.editButton.text = if (isEditing) "완료" else "편집"
        }

        // Search icon click listener
        binding.searchIcon.setOnClickListener {
            performSearch()
        }

        // 검색창
        binding.searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchEditText.text.toString()
                homeViewModel.searchProblems(query, null, refreshToken.toString()) // 전체 문제 검색
                true
            } else {
                false
            }
        }

        return binding.root
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()

        // 쿼리가 비어 있지 않을 때만 검색을 수행
        if (query.isNotEmpty()) {
            if (mypageViewModel.checkSubscriptionStatus() == true) { // 구독 여부를 확인
                val token = mypageViewModel.getRefreshToken().toString() // 토큰 가져오기
                homeViewModel.searchProblems(query, null, token) // 전체 문제 검색
                Log.d("HomeFragment", "Search performed with query: $query")
            } else {
                Toast.makeText(requireContext(), "구독 계정만 검색이 가능합니다.", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "Search blocked due to lack of subscription")
            }
        } else {
            Log.d("HomeFragment", "Search query is empty")
        }
    }

    private fun observeViewModel() {
        homeViewModel.notes.observe(viewLifecycleOwner, { notes ->
            adapter.updateNotes(notes)
            updateEmptyViewVisibility(notes.isEmpty())
        })
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun onCategoryClick(note: Note) {
        // 로그에 폴더 ID 출력
        Log.d("HomeFragment", "폴더 이동 : ${note.folderId}")

        // 폴더 상세 페이지로 이동
        val bundle = Bundle().apply {
            putInt("folderId", note.folderId)
            putString("folderName", note.title)
        }
        val navController = findNavController()
        Log.d("HomeFragment", "Navigating to HomeDetailFragment with folderId: ${note.folderId}")
        navController.navigate(R.id.action_navigation_home_to_homeDetailFragment, bundle)
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            notes = mutableListOf(),
            onItemClick = { note ->
                Log.d("HomeFragment", "아이템 클릭됨: ${note.title}")
                onCategoryClick(note)
            },
            onDeleteClick = { note ->
                showDeleteConfirmationDialog(note)
            },
            onFolderNameChanged = { note ->
                homeViewModel.updateFolderName(note.folderId, note.title, mypageViewModel.getRefreshToken().toString())
            },
            onOrderChanged = { updatedNotes ->
                // 서버에 폴더 순서 업데이트 요청
                val order = updatedNotes.map { it.folderId }
                val token = mypageViewModel.getRefreshToken().toString()
                homeViewModel.reorderFolders(token, order)
            },
            isEditing = isEditing
        )
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter

        // ItemTouchHelper를 사용하여 드래그 앤 드롭 기능 추가
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.moveItem(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // 좌우 스와이프를 통한 삭제 등 기능이 필요하다면 여기서 처리
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    //폴더 삭제
    private fun showDeleteConfirmationDialog(note: Note) {
        AlertDialog.Builder(requireContext())
            .setMessage("폴더를 삭제하시겠습니까?\n삭제하면 해당 폴더는 복구하기 어렵습니다.")
            .setPositiveButton("확인") { dialog, _ ->
                homeViewModel.deleteFolder(note.folderId, mypageViewModel.getRefreshToken().toString())
                Toast.makeText(requireContext(), "폴더가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // 대화상자 닫기
                homeViewModel.fetchFolders(refreshToken.toString()) //연결 재설정
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss() // 대화상자 닫기
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val refreshToken = mypageViewModel.getRefreshToken()
        if (refreshToken != null) {
            //homeViewModel.fetchFolders(refreshToken)
        }
    }


}