package io.github.omisie11.spacexfollower.ui.company


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

import io.github.omisie11.spacexfollower.R
import io.github.omisie11.spacexfollower.data.CompanyRepository
import io.github.omisie11.spacexfollower.data.model.Company
import kotlinx.android.synthetic.main.fragment_company.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class CompanyFragment : Fragment() {

    private val repository: CompanyRepository by inject()
    private val viewModel: CompanyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_company, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getCompanyInfo().observe(viewLifecycleOwner, Observer<Company> { companyInfo ->
            text_company.text = companyInfo?.headquarters?.address
        })

        // Observe if data is refreshing and show/hide loading indicator
        viewModel.getCompanyInfoLoadingStatus()
            .observe(viewLifecycleOwner, Observer<Boolean> { isCompanyInfoRefreshing ->
                swipeRefreshLayout.isRefreshing = isCompanyInfoRefreshing
            })

        // Show a snackbar whenever the [ViewModel.snackbar] is updated a non-null value
        viewModel.snackbar.observe(this, Observer { text ->
            text?.let {
                Snackbar.make(swipeRefreshLayout, text, Snackbar.LENGTH_LONG).setAction(
                    getString(R.string.snackbar_action_retry), View.OnClickListener {
                        viewModel.refreshCompanyInfo()
                    }).show()
                viewModel.onSnackbarShown()
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            Log.i("CompanyInfoFragment", "onRefresh called from SwipeRefreshLayout")
            viewModel.refreshCompanyInfo()
        }

        refresh_button.setOnClickListener {
            viewModel.refreshCompanyInfo()
        }

        delete_button.setOnClickListener {
            repository.deleteCompanyInfo()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshIfCompanyDataOld()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_action_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.action_refresh -> {
            viewModel.refreshCompanyInfo()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
