package co.touchlab.droidconandroid

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class VoteIntroFragment : Fragment() {
    private var mListener: OnIntroListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_vote_intro, container, false)

        var continueButton = view.findViewById(R.id.continueButton) as Button

        continueButton.setOnClickListener {
            if (mListener != null) {
                mListener!!.onIntroDone();
            }
        }

        return view
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mListener = activity as OnIntroListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                    activity.toString() + " must implement OnIntroListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnIntroListener {
        fun onIntroDone()
    }

    companion object {
        val Tag: String = "VoteIntroFragment"

        fun newInstance(): VoteIntroFragment {
            val fragment = VoteIntroFragment()
            return fragment
        }
    }

}
