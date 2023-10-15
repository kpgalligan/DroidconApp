package co.touchlab.droidconandroid

import android.app.SearchManager
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.touchlab.android.threading.eventbus.EventBusExt
import co.touchlab.android.threading.tasks.TaskQueue
import co.touchlab.droidconandroid.data.AppPrefs
import co.touchlab.droidconandroid.data.UserAccount
import co.touchlab.droidconandroid.tasks.AbstractFindUserTask
import co.touchlab.droidconandroid.tasks.FindUserTask
import co.touchlab.droidconandroid.utils.EmojiUtil
import co.touchlab.droidconandroid.utils.Toaster
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.wnafee.vector.compat.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_user_detail.*
import org.apache.commons.lang3.StringUtils

/**
 * Created by kgalligan on 7/27/14.
 */

private const val TWITTER_PREFIX: String = "http://www.twitter.com/"
private const val GPLUS_PREFIX: String = "http://www.google.com/+"
private const val LINKEDIN_PREFIX: String = "http://www.linkedin.com/in/"
private const val FACEBOOK_PREFIX: String = "http://www.facebook.com/"
private const val PHONE_PREFIX: String = "tel:"

class UserDetailFragment() : Fragment()
{
    companion object
    {
        val TAG: String = UserDetailFragment::class.java.simpleName

        interface FinishListener
        {
            fun onFragmentFinished()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        EventBusExt.getDefault().register(this)
        TaskQueue.loadQueueNetwork(activity).execute(FindUserTask(findUserCodeArg()))
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater !!.inflate(R.layout.fragment_user_detail, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = ""
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        EventBusExt.getDefault().unregister(this)
    }

    private fun findUserCodeArg(): String
    {
        var userCode = arguments?.getString(UserDetailActivity.USER_CODE)
        if (StringUtils.isEmpty(userCode))
        {
            userCode = activity.intent.getStringExtra(UserDetailActivity.USER_CODE)
        }

        if (StringUtils.isEmpty(userCode))
            throw IllegalArgumentException("Must set user code")

        return userCode !!
    }

    fun onEventMainThread(findUserTask: AbstractFindUserTask)
    {
        if (findUserTask.isError)
        {
            Toaster.showMessage(activity, getString(R.string.network_error))

            if (activity is UserDetailActivity)
                (activity as UserDetailActivity).onFragmentFinished()
        }
        else
        {
            val userAccount = findUserTask.user !!
            showUserData(userAccount)
        }
    }

    private fun showUserData(userAccount: UserAccount)
    {
        val avatarKey = userAccount.avatarImageUrl()
        if (! TextUtils.isEmpty(avatarKey))
        {
            val callback = object : Callback
            {
                override fun onSuccess()
                {
                    if(placeholder_emoji != null)
                       placeholder_emoji.text = ""
                }

                override fun onError()
                {
                    if(placeholder_emoji != null)
                        placeholder_emoji.text = EmojiUtil.getEmojiForUser(userAccount.name)
                }
            }

            Picasso.with(activity)
                    .load(avatarKey)
                    .placeholder(R.drawable.circle_profile_placeholder)
                    .into(profile_image, callback)
        }
        else
        {
            placeholder_emoji.text = EmojiUtil.getEmojiForUser(userAccount.name)
        }

        val iconsDefaultColor = ContextCompat.getColor(activity, R.color.social_icons)

        makeIconsPretty(iconsDefaultColor)

        if (! TextUtils.isEmpty(userAccount.name))
        {
            name.text = userAccount.name
        }

        if (! TextUtils.isEmpty(userAccount.phone))
        {
            phone.text = userAccount.phone
            phone.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse(PHONE_PREFIX + userAccount.phone)
                if (intent.resolveActivity(activity.packageManager) != null)
                {
                    startActivity(intent)
                }
            }
            phone.visibility = View.VISIBLE
        }
        else

        if (! TextUtils.isEmpty(userAccount.email) && userAccount.emailPublic != null && userAccount.emailPublic)
        {
            email.text = userAccount.email

            email.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", userAccount.email, null))
                startActivity(emailIntent)
            }
            email.visibility = View.VISIBLE
        }

        if (! TextUtils.isEmpty(userAccount.company))
        {
            company.text = userAccount.company
            company.visibility = View.VISIBLE

            company2.text = userAccount.company
            company2.visibility = View.VISIBLE
            company2.setOnClickListener {
                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                val keyword = userAccount.company
                intent.putExtra(SearchManager.QUERY, keyword)
                startActivity(intent)
            }
        }

        val facebookAccount = userAccount.facebook
        if (! TextUtils.isEmpty(facebookAccount))
        {
            facebook.text = facebookAccount
            facebook.setOnClickListener {
                openLink(Uri.parse(FACEBOOK_PREFIX + facebookAccount))
            }
            facebook.visibility = View.VISIBLE
        }

        var twitterAccount = userAccount.twitter
        if (! TextUtils.isEmpty(twitterAccount))
        {
            twitterAccount = twitterAccount.replace("@", "")
            twitter.text = "@$twitterAccount"
            twitter.setOnClickListener {
                openLink(Uri.parse(TWITTER_PREFIX + twitterAccount))
            }
            twitter.visibility = View.VISIBLE
        }

        val linkedInAccount = userAccount.linkedIn
        if (! TextUtils.isEmpty(linkedInAccount))
        {
            linkedIn.text = linkedInAccount
            linkedIn.setOnClickListener {
                openLink(Uri.parse(LINKEDIN_PREFIX + linkedInAccount))
            }
            linkedIn.visibility = View.VISIBLE
        }

        var gPlusAccount = userAccount.gPlus
        if (! TextUtils.isEmpty(gPlusAccount))
        {
            gPlusAccount = gPlusAccount.replace("+", "")
            gPlus.text = "+$gPlusAccount"
            gPlus.setOnClickListener {
                openLink(Uri.parse(GPLUS_PREFIX + gPlusAccount))
            }
            gPlus.visibility = View.VISIBLE
        }

        if (! TextUtils.isEmpty(userAccount.website))
        {
            website.text = userAccount.website
            website.setOnClickListener {
                var url = userAccount.website

                if (! url.startsWith("http://"))
                {
                    url = "http://" + url
                }
                openLink(Uri.parse(url))
            }
            website.visibility = View.VISIBLE
        }

        if (! TextUtils.isEmpty(userAccount.profile))
        {

            bio.text = Html.fromHtml(StringUtils.trimToEmpty(userAccount.profile)!!)
            bio.visibility = View.VISIBLE
        }


        val appPrefs = AppPrefs.getInstance(activity)
        if (! userAccount.id.equals(appPrefs.userId))
        {
            addContact.setOnClickListener {
                // Creates a new Intent to insert a contact
                val intent = Intent(ContactsContract.Intents.Insert.ACTION)
                // Sets the MIME type to match the Contacts Provider
                intent.type = ContactsContract.RawContacts.CONTENT_TYPE
                if (userAccount.emailPublic != null && userAccount.emailPublic)
                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL, userAccount.email)
                intent.putExtra(ContactsContract.Intents.Insert.COMPANY, userAccount.company)
                intent.putExtra(ContactsContract.Intents.Insert.NAME, userAccount.name)
                startActivity(intent)
            }
            addContact.visibility = View.VISIBLE
        }
    }

    private fun openLink(webPage: Uri?)
    {
        val intent = Intent(Intent.ACTION_VIEW, webPage)
        if (intent.resolveActivity(activity.packageManager) != null)
        {
            startActivity(intent)
        }
    }

    private fun makeIconsPretty(darkVibrantColor: Int)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            val contactDrawable = ResourcesCompat.getDrawable(activity,
                    R.drawable.vic_person_add_black_24dp)
            contactDrawable.colorFilter = PorterDuffColorFilter(darkVibrantColor,
                    PorterDuff.Mode.SRC_IN)
            addContact.setCompoundDrawablesWithIntrinsicBounds(contactDrawable, null, null, null)

            val phoneDrawable = ResourcesCompat.getDrawable(activity,
                    R.drawable.vic_phone_black_24dp)
            phoneDrawable.colorFilter = PorterDuffColorFilter(darkVibrantColor,
                    PorterDuff.Mode.SRC_IN)
            phone.setCompoundDrawablesWithIntrinsicBounds(phoneDrawable, null, null, null)

            val emailDrawable = ResourcesCompat.getDrawable(activity,
                    R.drawable.vic_email_black_24dp)
            emailDrawable.colorFilter = PorterDuffColorFilter(darkVibrantColor,
                    PorterDuff.Mode.SRC_IN)
            email.setCompoundDrawablesWithIntrinsicBounds(emailDrawable, null, null, null)

            val companyDrawable = ResourcesCompat.getDrawable(activity,
                    R.drawable.vic_company_black_24dp)
            companyDrawable.colorFilter = PorterDuffColorFilter(darkVibrantColor,
                    PorterDuff.Mode.SRC_IN)
            company2.setCompoundDrawablesWithIntrinsicBounds(companyDrawable, null, null, null)

            val websiteDrawable = ResourcesCompat.getDrawable(activity,
                    R.drawable.vic_website_black_24dp)
            websiteDrawable.colorFilter = PorterDuffColorFilter(darkVibrantColor,
                    PorterDuff.Mode.SRC_IN)
            website.setCompoundDrawablesWithIntrinsicBounds(websiteDrawable, null, null, null)

            val bioDrawable = ResourcesCompat.getDrawable(activity, R.drawable.vic_bio_black_24dp)
            bioDrawable.colorFilter = PorterDuffColorFilter(darkVibrantColor,
                    PorterDuff.Mode.SRC_IN)
            bio.setCompoundDrawablesWithIntrinsicBounds(bioDrawable, null, null, null)
        }
    }

}