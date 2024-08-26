package com.wash.washandroid.presentation.fragment.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.model.PaymentCallback
import com.tosspayments.paymentsdk.model.PaymentWidgetStatusListener
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.PaymentMethod
import com.wash.washandroid.R
import com.wash.washandroid.databinding.FragmentTosspaymentBinding

class TosspaymentFragment: Fragment() {

    private var _binding: FragmentTosspaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTosspaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paymentWidget = PaymentWidget(
            activity = AppCompatActivity(), // Fragment에서 Activity를 가져오기 위해 AppCompatActivity() 사용
            clientKey = "test_ck_Ba5PzR0Arnwe4Pbpnqdv8vmYnNeD",
            customerKey = "GVzdF9za19RYmdNR1p6b3J6bUVwWG5OQXgwRFZsNUUxZW00Cg==",
        )

        val paymentMethodWidgetStatusListener = object : PaymentWidgetStatusListener {
            override fun onLoad() {
                val message = "결제위젯 렌더링 완료"
                Log.d("PaymentWidgetStatusListener", message)
            }
            override fun onFail(fail: TossPaymentResult.Fail) {
                // 렌더링 실패 처리
                Log.e("PaymentWidgetStatusListener", "결제위젯 렌더링 실패: ${fail.errorMessage}")
            }
        }

        paymentWidget.run {
            renderPaymentMethods(
                method = binding.paymentMethodWidget, // Fragment의 binding 객체를 사용
                amount = PaymentMethod.Rendering.Amount(10000),
                paymentWidgetStatusListener = paymentMethodWidgetStatusListener
            )

            renderAgreement(binding.agreementWidget)
        }

        binding.requestPaymentCta.setOnClickListener {
            paymentWidget.requestPayment(
                paymentInfo = PaymentMethod.PaymentInfo(orderId = "wBWO9RJXO0UYqJMV4er8J", orderName = "orderName"),
                paymentCallback = object : PaymentCallback {
                    override fun onPaymentSuccess(success: TossPaymentResult.Success) {
                        Log.i("TosspaymentFragment", success.paymentKey)
                        Log.i("TosspaymentFragment", success.orderId)
                        Log.i("TosspaymentFragment", success.amount.toString())
                    }

                    override fun onPaymentFailed(fail: TossPaymentResult.Fail) {
                        Log.e("TosspaymentFragment", fail.errorMessage)
                    }
                }
            )
            findNavController().navigate(R.id.navigation_subscribe)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}