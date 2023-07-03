package com.android.elit.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.FragmentAccountBinding
import com.android.elit.repository.UserRepository
import com.android.elit.ui.activity.ChangePasswordActivity
import com.android.elit.ui.activity.LoginActivity
import com.android.elit.ui.activity.UpdateProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.*


class AccountFragment : Fragment() {

    private lateinit var _binding: FragmentAccountBinding
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var documentRef: DocumentReference
    private lateinit var listenerRegistration: ListenerRegistration
    private val usersRepository = UserRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountBinding.bind(view)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()
        binding.tvNameOfUser.text = null

        versionApp()
        actionBar()
        setGreeting()
        setName()
        chooseOption()
        autoRefreshUI()
        logout()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n")
    private fun versionApp(){
        val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, PackageManager.GET_ACTIVITIES)
        val versionName = packageInfo.versionName

        binding.tvVersion.text = getString(R.string.version)+" "+versionName
    }

    private fun actionBar(){
        binding.apply {
            ivBackToolbar.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun getGreeting(hour: Int): String{
        return when (hour){
            in 0..11 -> getString(R.string.good_morning)
            in 12..15 -> getString(R.string.good_afternoon)
            in 16..20 -> getString(R.string.good_evening)
            in 21..23 -> getString(R.string.good_night)
            else -> getString(R.string.greeting)
        }
    }

    private fun setGreeting(){
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val greetingText = getGreeting(currentHour)
        binding.tvGreeting.text = greetingText
    }

    private fun setName(){
        val userId = auth.currentUser?.uid
        val userRef = usersRepository.getUserById(userId!!)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()){
                val name = document.getString("fullname")
                binding.tvNameOfUser.text = name
                loadingDialog.dismiss()
            }
        }
    }

    private fun chooseOption(){
        binding.apply {
            tvUpdateProfile.setOnClickListener {
                val intent = Intent(requireContext(), UpdateProfileActivity::class.java)
                startActivity(intent)
            }
            tvChangePassword.setOnClickListener{
                val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
                startActivity(intent)
            }
            tvChangeLanguage.setOnClickListener {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun logout(){
        binding.btnLogout.setOnClickListener {
            val user = auth.currentUser
            if (user != null) {
                auth.signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun autoRefreshUI(){
        val userId = auth.currentUser?.uid
        documentRef = usersRepository.getUserById(userId!!)
        listenerRegistration = documentRef.addSnapshotListener { snapshot, error ->
            if (error != null){
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()){
                val name = snapshot.getString("fullname")
                binding.tvNameOfUser.text = name
            }
        }
    }
}