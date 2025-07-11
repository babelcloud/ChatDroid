package ai.gbox.chatdroid.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ai.gbox.chatdroid.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HomeFragment", "Creating HomeFragment view")
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adapter = ChatListAdapter()
        binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChats.adapter = adapter

        homeViewModel.chats.observe(viewLifecycleOwner) { chatList ->
            Log.d("HomeFragment", "Received chat list with ${chatList.size} items")
            adapter.update(chatList)
            updateEmptyState(chatList.isEmpty(), false, null)
        }
        
        homeViewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                Log.e("HomeFragment", "Error observed: $it")
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
                updateEmptyState(true, false, "Error: $it")
            }
        }

        homeViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("HomeFragment", "Loading state: $isLoading")
            if (isLoading) {
                updateEmptyState(true, true, null)
            }
        }

        return root
    }

    private fun updateEmptyState(isEmpty: Boolean, isLoading: Boolean, errorMessage: String?) {
        if (isLoading) {
            binding.tvEmptyMessage.text = "Loading chats..."
            binding.tvEmptyMessage.visibility = View.VISIBLE
        } else if (errorMessage != null) {
            binding.tvEmptyMessage.text = errorMessage
            binding.tvEmptyMessage.visibility = View.VISIBLE
        } else if (isEmpty) {
            binding.tvEmptyMessage.text = "No chats available. Start a new conversation!"
            binding.tvEmptyMessage.visibility = View.VISIBLE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}