package com.chatsoone.rechat.ui.setting

import android.util.Log
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.data.entity.PrivacyInformation
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.ActivityPrivacyInformationBinding

class PrivacyInformationActivity: BaseActivity<ActivityPrivacyInformationBinding>(ActivityPrivacyInformationBinding::inflate) {
    private lateinit var database: AppDatabase
    private lateinit var privacyInformationRVAdapter: PrivacyInformationRVAdapter
    private lateinit var privacyInformationList: ArrayList<PrivacyInformation>

    private val tag = "ACT/PRIVATE-INFORMATION"

    override fun afterOnCreate() {
        initPrivacyInformation()
        initRecyclerView()
        initClickListener()
    }

    private fun initPrivacyInformation() {
        database = AppDatabase.getInstance(this)!!
        privacyInformationList = database.privacyInformationDao().getPrivacyInformation() as ArrayList
        Log.d(tag, "initPrivacyInformation()/privacyInformationList.size: ${privacyInformationList.size}")

        if(privacyInformationList.size == 0) {
            database.privacyInformationDao().insert(PrivacyInformation(
                "가. 수집하는 개인정보의 항목 및 수집방법",
                "RE: CHAT앱(이하 리챗)은 개인을 식별할 수 있는 개인정보를 수집하지 않습니다.\n" +
                        "단, 제 3자의 서비스인 광고 및 분석 서비스에 의해 광고 식별자인 Android 광고 ID가 수집될 수도 있습니다. 광고 식별자는 미 영구적이고 비 개인적인 식별자로써 개인을 식별할 수 없으며 식별한 정보를 별도로 수집, 보관 하지 않습니다."
            ))

            database.privacyInformationDao().insert(
                PrivacyInformation(
                "나. 개인정보의 수집 및 이용목적메세지",
                    "RE: CHAT(이하 리챗)은 개인정보를 수집하지 않습니다."
            ))

            database.privacyInformationDao().insert(PrivacyInformation(
                "다. 개인정보 제공 및 공유",
                "RE CHAT앱(이하 리챗)은 개인정보를 공유 및 제3자에게 제공하지 않습니다."
            ))

            database.privacyInformationDao().insert(PrivacyInformation(
                "라. 수집한 개인정보의 취급 위탁",
                "RE: CHAT앱(이하 리챗)은 개인정보를 수집, 이용, 보유하지 않습니다."
            ))

            database.privacyInformationDao().insert(PrivacyInformation(
                "마. 개인정보의 보유 및 이용기간",
                "RE: CHAT앱(이하 리챗)은 보유할 개인정보가 없습니다."
            ))

            database.privacyInformationDao().insert(PrivacyInformation(
                "바. 개인정보 파기절차 및 방법",
                "RE: CHAT 앱(이하 리챗)은 파기할 개인정보가 없습니다."
            ))

            database.privacyInformationDao().insert(PrivacyInformation(
                "사. 개인정보 자동 수집 장치의 설치․운영 및 거부에 관한 사항",
            "RE: CHAT앱(이하 리챗)은 개인정보 자동 수집 장치를 운영하지 않습니다."
            ))

            database.privacyInformationDao().insert(PrivacyInformation(
                "아. 개인정보관리책임자",
                "RE: CHAT앱(이하 리챗)은 고객의 개인정보를 보호하고 개인정보와 관련한 불만을 처리하기 위하여 아래와 같이 관련 부서 및 개인정보관리책임자를 지정하고 있습니다.\n" +
                        "성명: 김민경\n" +
                        "직책: RE: CHAT 관리자\n" +
                        "연락처: atleastigotta@gmail.com"
            ))

            database.privacyInformationDao().insert(PrivacyInformation(
                null,
                "귀하께서는 RE: CHAT의 서비스를 이용하시며 발생하는 모든 개인정보보호 관련 민원을 개인정보관리책임자에게 신고하실 수 있습니다. RE:CHAT 앱은 이용자들의 신고사항에 대해 신속하게 충분한 답변을 드릴 것입니다.\n" +
                        "기타 개인정보침해에 대한 신고나 상담이 필요하신 경우에는 아래 기관에 문의하시기 바랍니다.\n" +
                        "1.개인분쟁조정위원회 (www.1336.or.kr/1336)\n" +
                        "2.정보보호마크인증위원회 (www.eprivacy.or.kr/02-580-0533~4)\n" +
                        "3.대검찰청 인터넷범죄수사센터 (http://icic.sppo.go.kr/02-3480-3600)\n" +
                        "4.경찰청 사이버테러대응센터 (www.ctrc.go.kr/02-392-0330)\n"
            ))

            database.privacyInformationDao().insert(PrivacyInformation(
                "자.",
                "개인정보 처리방침 변경일 개인정보처리방침은 시행일로부터 적용되며, 법령 및 방침에 따른 변경내용의 추가, 삭제 및 정정이 있는 경우에는 변경사항의 시행 전 또는 후 어플리케이션 고객센터/도움말을 통하여 고지할 것입니다. 이 개인정보처리방침은 2022년 2월 25일부터 적용 됩니다."
            ))

            privacyInformationList = database.privacyInformationDao().getPrivacyInformation() as ArrayList
            Log.d(tag, "initPrivacyInformation()/privacyInformationList: $privacyInformationList")
        }
    }

    private fun initRecyclerView() {
        privacyInformationRVAdapter = PrivacyInformationRVAdapter(privacyInformationList)
        binding.privacyInformationRecyclerView.adapter = privacyInformationRVAdapter
    }

    private fun initClickListener() {
        binding.privacyInformationExitBtnIv.setOnClickListener {
            Log.d(tag, "initClickListener()/privacyInformationExitBtnIv.setOnClickListener")
            finish()
        }
    }
}