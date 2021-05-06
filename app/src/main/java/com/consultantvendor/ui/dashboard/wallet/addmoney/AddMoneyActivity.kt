package com.consultantvendor.ui.dashboard.wallet.addmoney

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.appClientDetails
import com.consultantvendor.data.models.responses.Wallet
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentAddMoenyBinding
import com.consultantvendor.ui.dashboard.wallet.WalletViewModel
import com.consultantvendor.ui.dashboard.wallet.addmoney.AddCardFragment.Companion.CARD_DETAILS
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.webview.WebViewActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import dagger.android.support.DaggerAppCompatActivity
import org.json.JSONObject
import javax.inject.Inject

class AddMoneyActivity : DaggerAppCompatActivity(), PaymentResultListener {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentAddMoenyBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: WalletViewModel

    private lateinit var adapter: CardsAdapter

    private var items = ArrayList<Wallet>()

    var selectedCardId = ""

    private var paymentFrom = PaymentFrom.STRIPE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_add_moeny)
        initialise()
        listeners()
        bindObservers()
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[WalletViewModel::class.java]
        progressDialog = ProgressDialog(this)
        binding.tvSymbol.text = getCurrencySymbol()

        when (paymentFrom) {
            PaymentFrom.STRIPE -> {
                binding.tvAddCard.visible()
                binding.tvSelectCard.visible()
                binding.rvListing.visible()
                setAdapter()
                viewModel.cardListing(HashMap())
            }
            PaymentFrom.RAZOR_PAY -> {
                binding.tvAddCard.gone()
                loadRazorPay()
            }
            PaymentFrom.CCA_VENUE -> {
                binding.tvAddCard.gone()
            }
        }
    }

    private fun setAdapter() {
        adapter = CardsAdapter(this, items)
        binding.rvListing.adapter = adapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.tvAddCard.setOnClickListener {
            disableButton(binding.tvAddCard)

            startActivityForResult(Intent(this, DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.ADD_CARD), AppRequestCode.ADD_CARD)

        }

        binding.tvAdd.setOnClickListener {
            disableButton(binding.tvAdd)
            if (binding.etAmount.text.toString().isEmpty() || binding.etAmount.text.toString()
                            .toInt() == 0
            ) {
                binding.etAmount.showSnackBar(getString(R.string.enter_amount))
            } else if (paymentFrom == PaymentFrom.STRIPE && selectedCardId.isEmpty()) {
                binding.etAmount.showSnackBar(getString(R.string.select_card))
            } else {
                if (isConnectedToInternet(this, true)) {
                    when (paymentFrom) {
                        PaymentFrom.STRIPE -> {
                            val hashMap = HashMap<String, Any>()

                            hashMap["balance"] = binding.etAmount.text.toString()
                            hashMap["card_id"] = selectedCardId
                            viewModel.addMoney(hashMap)
                        }
                        PaymentFrom.RAZOR_PAY -> {
                            val hashMap = HashMap<String, String>()

                            val amount = (binding.etAmount.text.toString().toInt()) * 100
                            hashMap["balance"] = amount.toString()
                            viewModel.razorPayCreateOrder(hashMap)
                        }

                        PaymentFrom.CCA_VENUE -> {
                            //startActivity(Intent(this, InitialScreenActivity::class.java))
                        }
                    }
                }
            }
        }

        binding.tvMoney1.setOnClickListener {
            updateMoney(500)
        }

        binding.tvMoney2.setOnClickListener {
            updateMoney(1000)
        }

        binding.tvMoney3.setOnClickListener {
            updateMoney(1500)
        }

    }

    private fun updateMoney(amount: Int) {
        val newAmount = if (binding.etAmount.text.toString().isEmpty())
            amount
        else
            binding.etAmount.text.toString().toInt() + amount

        if (newAmount.toString().length < 5) {
            binding.etAmount.setText(newAmount.toString())
        }
    }


    private fun bindObservers() {
        viewModel.addMoney.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    /*If need authontication*/
                    if (it.data?.requires_source_action == true) {
                        startActivityForResult(Intent(this, WebViewActivity::class.java)
                                .putExtra(WebViewActivity.LINK_TITLE, getString(R.string.payment))
                                .putExtra(WebViewActivity.PAYMENT_URL, it.data.url)
                                .putExtra(EXTRA_REQUEST_ID, it.data.transaction_id), AppRequestCode.ADD_MONEY)
                    } else {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }

                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModel.cardListing.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    items.clear()
                    items.addAll(it.data?.cards ?: emptyList())
                    if (items.isNotEmpty()) {
                        items[0].isSelected = true
                        selectedCardId = items[0].id ?: ""
                    }

                    adapter.notifyDataSetChanged()


                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    adapter.setAllItemsLoaded(true)

                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })

        viewModel.orderCreate.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    if (it.data?.order_id?.isNotEmpty() == true)
                        startRazorPayPayment(binding.etAmount.text.toString(), it.data.order_id
                                ?: "")
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModel.deleteCard.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    viewModel.cardListing(HashMap())
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })
    }

    fun editCard(item: Wallet) {
        startActivityForResult(Intent(this, DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, DrawerActivity.ADD_CARD)
                .putExtra(CARD_DETAILS, item), AppRequestCode.ADD_MONEY)
    }

    fun deleteCard(item: Wallet) {
        AlertDialogUtil.instance.createOkCancelDialog(
                this, R.string.delete,
                R.string.delete_message, R.string.delete, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        if (isConnectedToInternet(this@AddMoneyActivity, true)) {
                            val hashMap = HashMap<String, Any>()

                            hashMap["card_id"] = item.id ?: ""
                            viewModel.deleteCard(hashMap)
                        }
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ADD_CARD) {
                viewModel.cardListing(HashMap())
            } else if (requestCode == AppRequestCode.ADD_MONEY) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }


    val TAG: String = AddMoneyActivity::class.toString()

    private fun loadRazorPay() {
        /*
        * To ensure faster loading of the Checkout form,
        * call this method as early as possible in your checkout flow
        * */
        Checkout.preload(this)
    }

    private fun startRazorPayPayment(amount: String, orderId: String) {
        /*
        *  You need to pass current activity in order to let Razorpay create CheckoutActivity
        * */
        val activity: Activity = this
        val co = Checkout()
        co.setKeyID(appClientDetails.razorKey ?: "rzp_test_NIJ8Fwm7fvVNDU")

        try {
            val userData = userRepository.getUser()

            val options = JSONObject()
            options.put("name", "Razorpay Corp")
            options.put("description", "Demoing Charges")
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("currency", "INR")
            options.put("order_id", orderId)
            options.put("amount", (amount.toInt() * 100))

            options.put("user_id", userData?.id)
            val prefill = JSONObject()
            prefill.put("email", userData?.email)
            prefill.put("contact", userData?.phone)

            options.put("prefill", prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        try {
            Toast.makeText(this,
                    "Payment failed $errorCode \n $response", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try {
            Toast.makeText(this, "Payment Successful $razorpayPaymentId",
                    Toast.LENGTH_LONG).show()

            setResult(Activity.RESULT_OK)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }
}