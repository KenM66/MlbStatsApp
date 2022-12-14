package com.example.mlbstatsapp.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mlbstatsapp.*
import com.example.mlbstatsapp.databinding.FragmentPlayerSearchResultsBinding
import com.google.gson.Gson
import java.lang.reflect.GenericArrayType

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlayerSearchResultsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayerSearchResultsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var sharedViewModel: HomePlayerSearchSharedViewModel
    lateinit var searchTerm:String

    lateinit var playerSearchResultsRecycler:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var bind:FragmentPlayerSearchResultsBinding = FragmentPlayerSearchResultsBinding.inflate(inflater, container, false)
        var view = bind.root

        val viewModel = makeApiCall(view)
        bind.setVariable(BR.playerSearchViewModel, viewModel)
        bind.executePendingBindings()

        playerSearchResultsRecycler = view.findViewById(R.id.playerSearchResultsRecyclerView)
        playerSearchResultsRecycler.apply {
            layoutManager = LinearLayoutManager(this.context)
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlayerSearchResultsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayerSearchResultsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    /**
     * In this method we make the api call to get the search results
     * We get the search value from the HomePlayerSearchSharedViewModel
     * We then create an PlayerSearchViewModel and pass that search value into getPlayerSearchList
     * We also observe playerSearchResultsData live mutable data list
     * When that data list has data put in we pass that data into the recyclerView via the view model
     */
    fun makeApiCall(view:View): PlayerSearchViewModel {
        var playerSearchView: PlayerSearchViewModel = PlayerSearchViewModel()
        sharedViewModel = activity?.run {
            ViewModelProviders.of(this).get(HomePlayerSearchSharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        sharedViewModel.playerSearchTerm.observe(viewLifecycleOwner, Observer {
             searchTerm = sharedViewModel.playerSearchTerm.value.toString()
            playerSearchView.getPlayerSearchResultsDataObserver().observe(viewLifecycleOwner,Observer<PlayerList>{
                if(it.totalSize.toInt() > 0){
                    Log.d(PlayerSearchResultsFragment::class.java.simpleName, it.row.toString())
                    var playerArrayList:ArrayList<PlayerApiModel> = ArrayList()
                    if(it.totalSize.toInt() > 1){
                        var genericArray = it.row as ArrayList<GenericArrayType>
                        for(i in 0..(genericArray.size)-1){
                            var json = Gson().toJson(genericArray.get(i))
                            var playerObjet = Gson().fromJson(json, PlayerApiModel::class.java)
                            playerArrayList.add(playerObjet)
                        }
                    }else{
                        var json = Gson().toJson(it.row)
                        var playerObject = Gson().fromJson(json, PlayerApiModel::class.java)
                        if(playerObject != null){
                            playerArrayList.add(playerObject)
                        }else{
                            Toast.makeText(view.context, "Could not find a player, please enter another query", Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d(PlayerSearchResultsFragment::class.java.simpleName, playerArrayList.toString())
                    playerSearchView.setAdapterData(playerArrayList)

                }else{
                    Log.d(PlayerSearchResultsFragment::class.java.simpleName, "SOMETHING WENT WRONG")
                    Toast.makeText(view.context, "Error something went wrong with data", Toast.LENGTH_SHORT).show()
                }
            })
            playerSearchView.getPlayerSearchList(searchTerm)
        })

        return playerSearchView
    }
}