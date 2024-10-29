package com.example.pennypulse
import com.auth0.android.jwt.JWT
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ProfileFragment : Fragment() {

    private var userName: String? = null
    private var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userName = it.getString(ARG_NAME)
            userToken = it.getString(ARG_TOKEN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find the TextView for the name and set it
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        nameTextView.text = userName
        val user: TextView = view.findViewById(R.id.userIdTextView)
        val temp= userToken?.let {
            JWT(it).getClaim("userId").asString()?.toIntOrNull()
        }.toString()
        user.text= temp.replaceFirstChar { char -> char.uppercase() }
        val email: TextView = view.findViewById(R.id.emailAddressTextView)
        email.text = userToken?.let {
            JWT(it).getClaim("email").asString()
        }.toString()

        return view
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_TOKEN = "token"

        @JvmStatic
        fun newInstance(name: String, token: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putString(ARG_TOKEN, token)
                }
            }
    }
}
