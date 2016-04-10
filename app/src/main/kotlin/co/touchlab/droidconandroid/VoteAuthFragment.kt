package co.touchlab.droidconandroid

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsSession
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.android.threading.tasks.utils.TaskQueueHelper
import co.touchlab.droidconandroid.tasks.CanUserVoteTask

class VoteAuthFragment : Fragment() {

    companion object {
        val Tag: String = "VoteAuthFragment"

        fun newInstance(): VoteAuthFragment {
            val fragment = VoteAuthFragment()
            return fragment
        }
    }

    private var mListener: OnAuthListener? = null

    val CLIENT_ID = "PZW4S6QLEXNRQJHY7Q"
    val LOGIN_URL = "https://www.eventbrite.com/oauth";

    var errorWrapper: View? = null
    var progressWrapper: View? = null
    var failureMessageWrapper: View? = null

    var getTicketButton: Button? = null
    var shareButton: Button? = null
    var retryButton: Button? = null


    var mCustomTabsSession: CustomTabsSession? = null;
    var mClient: CustomTabsClient? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_vote_auth, container, false)

        var authButton = view.findViewById(R.id.event_auth) as Button
        authButton.setOnClickListener {
            onEbAuthClicked()
        }

        errorWrapper = view.findViewById(R.id.error_wrapper)
        progressWrapper = view.findViewById(R.id.progress_wrapper)
        failureMessageWrapper = view.findViewById(R.id.auth_fail_wrapper)

        getTicketButton = view.findViewById(R.id.getTicketButton) as Button
        getTicketButton!!.setOnClickListener {
            var builder = CustomTabsIntent.Builder(getSession());
            builder.setToolbarColor(ContextCompat.getColor(context, R.color.primary)).setShowTitle(true);
            builder.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left);
            builder.setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right);
            var customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getActivity(), Uri.parse("http://www.eventbrite.com/e/droidcon-san-francisco-2016-tickets-18125767659"));
        }

        shareButton = view.findViewById(R.id.shareButton) as Button
        shareButton!!.setOnClickListener {
            var intent = Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "http://www.eventbrite.com/e/droidcon-san-francisco-2016-tickets-18125767659");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Droidcon Tickets");
            startActivity(Intent.createChooser(intent, "Share"));
        }

        retryButton = view.findViewById(R.id.retryButton) as Button
        retryButton!!.setOnClickListener {
            TaskQueue.loadQueueDefault(context.applicationContext).execute(CanUserVoteTask(PlatformClientContainer.platformClient))
            showHideProgress()
        }


        EventBusExt.getDefault().register(this)
        TaskQueue.loadQueueDefault(context.applicationContext).execute(CanUserVoteTask(PlatformClientContainer.platformClient))

        showHideProgress()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBusExt.getDefault().unregister(this)
    }


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mListener = activity as OnAuthListener
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun getSession(): CustomTabsSession? {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient!!.newSession(null)
        }
        return mCustomTabsSession;
    }

    private fun onEbAuthClicked() {
        var auth_dialog = Dialog(activity);
        auth_dialog.setContentView(R.layout.auth_dialog);
        var web = auth_dialog.findViewById(R.id.webv) as WebView;
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(LOGIN_URL + "/authorize" + "?client_id=" + CLIENT_ID + "&response_type=code");
        web.setWebViewClient(object : WebViewClient() {

            var authComplete = false;

            override public fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url);

                var authCode: String
                if (url.contains("?code=") && authComplete != true) {
                    var uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    authComplete = true;

                    auth_dialog.dismiss();

                    TaskQueue.loadQueueDefault(activity.applicationContext).execute(CanUserVoteTask(authCode, PlatformClientContainer.platformClient))
                    showHideProgress()
                } else if (url.contains("error=access_denied")) {
                    authComplete = true;
                    Toast.makeText(activity.getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                    auth_dialog.dismiss();
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize with Eventbrite");
        auth_dialog.setCancelable(true);
    }

    private fun showHideProgress(failed: Boolean = false) {
        if (failed) {
            errorWrapper!!.setVisibility(View.VISIBLE)
            progressWrapper!!.setVisibility(View.GONE)
            failureMessageWrapper!!.setVisibility(View.GONE)
        } else if (TaskQueueHelper.hasTasksOfType(TaskQueue.loadQueueDefault(activity.applicationContext), javaClass<CanUserVoteTask>())) {
            errorWrapper!!.setVisibility(View.GONE)
            progressWrapper!!.setVisibility(View.VISIBLE)
            failureMessageWrapper!!.setVisibility(View.GONE)
        } else {
            errorWrapper!!.setVisibility(View.GONE)
            progressWrapper!!.setVisibility(View.GONE)
            failureMessageWrapper!!.setVisibility(View.VISIBLE)
        }
    }

    //------------------EVENTBUS
    public fun onEventMainThread(task: CanUserVoteTask) {
        if (task.failed) {
            showHideProgress(true)
        } else if (task.canVote) {
            if (mListener != null)
                mListener!!.onAuthSuccessful()
        } else {
            showHideProgress()
        }
    }

    //-------------------INTERFACE
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnAuthListener {
        fun onAuthSuccessful()
    }

}
