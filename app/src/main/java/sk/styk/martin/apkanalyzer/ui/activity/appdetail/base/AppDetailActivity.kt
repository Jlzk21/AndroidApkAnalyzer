package sk.styk.martin.apkanalyzer.ui.activity.appdetail.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_app_detail.*
import sk.styk.martin.apkanalyzer.R
import sk.styk.martin.apkanalyzer.ui.activity.appdetail.pager.AppDetailPagerContract.Companion.ARG_PACKAGE_NAME
import sk.styk.martin.apkanalyzer.ui.activity.appdetail.pager.AppDetailPagerContract.Companion.ARG_PACKAGE_PATH
import sk.styk.martin.apkanalyzer.ui.activity.appdetail.pager.AppDetailPagerFragment
import sk.styk.martin.apkanalyzer.util.AdUtils

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [AppListFragment].
 *
 * @author Martin Styk
 */
class AppDetailActivity : AppCompatActivity(), AppDetailActivityContract.View {

    private lateinit var presenter: AppDetailActivityContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_detail)

        presenter = AppDetailActivityPresenter()
        presenter.view = this
        presenter.initialize()
    }

    override fun setupViews() {
        setSupportActionBar(detail_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val existingAppDetailPagerFragment = supportFragmentManager.findFragmentByTag(AppDetailPagerFragment.TAG)
        if (existingAppDetailPagerFragment == null) {

            val detailFragment = AppDetailPagerFragment.create(
                    packageName = intent.getStringExtra(ARG_PACKAGE_NAME),
                    packageUri = intent.getParcelableExtra(ARG_PACKAGE_PATH))

            supportFragmentManager.beginTransaction().add(sk.styk.martin.apkanalyzer.R.id.item_detail_container, detailFragment, AppDetailPagerFragment.TAG)
                    .commit()
        }

        // this happens only in tablet mode when this activity is rotated from horizontal to vertical orientation
        if (btn_actions == null) {
            app_bar.setExpanded(false)
        } else {
            btn_actions?.apply {
                setOnClickListener {
                    //delegate to fragment
                    (supportFragmentManager.findFragmentByTag(AppDetailPagerFragment.TAG) as AppDetailPagerFragment).presenter.actionButtonClick()
                }
            }
        }

        AdUtils.displayAd(ad_view, object : AdUtils.AdLoadedListener {
            override fun onAdLoaded() {
                val displayHeight = resources.displayMetrics.heightPixels / resources.displayMetrics.density
                val bannerHeight = when {
                    displayHeight <= 400 -> resources.getDimensionPixelOffset(R.dimen.ad_banner_height_small)
                    displayHeight > 400 && displayHeight < 720 -> resources.getDimensionPixelOffset(R.dimen.ad_banner_height_medium)
                    else -> resources.getDimensionPixelOffset(R.dimen.ad_banner_height_large)
                }

                item_detail_container.setPadding(0, 0, 0, bannerHeight)
            }
        })

    }

    public override fun onPause() {
        if (AdUtils.isAdEnabled) ad_view?.pause()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        if (AdUtils.isAdEnabled) ad_view?.resume()
    }

    public override fun onDestroy() {
        if (AdUtils.isAdEnabled) ad_view?.destroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun createIntent(packageName: String? = null, packageUri: Uri? = null, context: Context) =
                Intent(context, AppDetailActivity::class.java).apply {
                    putExtra(ARG_PACKAGE_NAME, packageName)
                    putExtra(ARG_PACKAGE_PATH, packageUri)
                }
    }
}

