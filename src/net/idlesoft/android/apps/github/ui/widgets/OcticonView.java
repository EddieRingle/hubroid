package net.idlesoft.android.apps.github.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PaintDrawable;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.idlesoft.android.apps.github.R;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public
class OcticonView extends RelativeLayout
{
	private static final float OCTICON_CORNER_RADIUS = 3;
	private static final String OCTICON_FONT = "octicons_regular.ttf";
	private static Typeface OCTICON_TYPE = null;

	public static final int MINI				= 0xf000;
	public static final int MEGA				= 0xf200;

	public static final int IC_PRIVATE_REPO		= 0x0000;
	public static final int IC_PUBLIC_REPO		= 0x0001;
	public static final int IC_REPO_FORKED		= 0x0002;
	public static final int IC_CREATE			= 0x0003;
	public static final int IC_DELETE			= 0x0004;
	public static final int IC_PUSH				= 0x0005;
	public static final int IC_PULL				= 0x0006;
	public static final int IC_WIKI				= 0x0007;
	public static final int IC_README			= 0x0007;
	public static final int IC_OCTOCAT			= 0x0008;
	public static final int IC_BLACKTOCAT		= 0x0009;
	public static final int IC_INVERTOCAT		= 0x000a;
	public static final int IC_DOWNLOAD			= 0x000b;
	public static final int IC_UPLOAD			= 0x000c;
	public static final int IC_KEYBOARD			= 0x000d;
	public static final int IC_GIST				= 0x000e;
	public static final int IC_GIST_PRIVATE		= 0x000f;
	public static final int IC_CODE_FILE		= 0x0010;
	public static final int IC_DOWNLOAD_UNKNOWN	= 0x0010;
	public static final int IC_TEXT_FILE		= 0x0011;
	public static final int IC_DOWNLOAD_TEXT	= 0x0011;
	public static final int IC_DOWNLOAD_MEDIA	= 0x0012;
	public static final int IC_DOWNLOAD_ZIP		= 0x0013;
	public static final int IC_DOWNLOAD_PDF		= 0x0014;
	public static final int IC_DOWNLOAD_TAG		= 0x0015;
	public static final int IC_DIRECTORY		= 0x0016;
	public static final int IC_SUBMODULE		= 0x0017;
	public static final int IC_PERSON			= 0x0018;
	public static final int IC_TEAM				= 0x0019;
	public static final int IC_MEMBER_ADDED		= 0x001a;
	public static final int IC_MEMBER_REMOVED	= 0x001b;
	public static final int IC_FOLLOW			= 0x001c;
	public static final int IC_WATCHING			= 0x001d;
	public static final int IC_UNWATCH			= 0x001e;
	public static final int IC_COMMIT			= 0x001f;
	public static final int IC_PUBLIC_FORK		= 0x0020;
	public static final int IC_FORK				= 0x0020;
	public static final int IC_PRIVATE_FORK		= 0x0021;
	public static final int IC_PULL_REQUEST		= 0x0022;
	public static final int IC_MERGE			= 0x0023;
	public static final int IC_PUBLIC_MIRROR	= 0x0024;
	public static final int IC_PRIVATE_MIRROR	= 0x0025;
	public static final int IC_ISSUE_OPENED		= 0x0026;
	public static final int IC_ISSUE_REOPENED	= 0x0027;
	public static final int IC_ISSUE_CLOSED		= 0x0028;
	public static final int IC_ISSUE_COMMENT	= 0x0029;
	public static final int IC_STAR				= 0x002a;
	public static final int IC_COMMIT_COMMENT	= 0x002b;
	public static final int IC_HELP				= 0x002c;
	public static final int IC_EXCLAMATION		= 0x002d;
	public static final int IC_SEARCH_INPUT		= 0x002e;
	public static final int IC_ADVANCED_SEARCH	= 0x002f;
	public static final int IC_NOTIFICATIONS	= 0x0030;
	public static final int IC_ACCOUNT_SETTINGS	= 0x0031;
	public static final int IC_LOGOUT			= 0x0032;
	public static final int IC_ADMIN_TOOLS		= 0x0033;
	public static final int IC_FEED				= 0x0034;
	public static final int IC_CLIPBOARD		= 0x0035; /* UNOFFICIAL NAME */
	public static final int IC_APPLE			= 0x0036;
	public static final int IC_WINDOWS			= 0x0037;
	public static final int IC_IOS				= 0x0038;
	public static final int IC_ANDROID			= 0x0039;
	public static final int IC_CONFIRM			= 0x003a;
	public static final int IC_UNREAD_NOTE		= 0x003b;
	public static final int IC_READ_NOTE		= 0x003c;
	public static final int IC_ARR_UP			= 0x003d;
	public static final int IC_ARR_RIGHT		= 0x003e;
	public static final int IC_ARR_DOWN			= 0x003f;
	public static final int IC_ARR_LEFT			= 0x0040;
	public static final int IC_PIN				= 0x0041;
	public static final int IC_GIFT				= 0x0042;
	public static final int IC_GRAPH			= 0x0043;
	public static final int IC_WRENCH			= 0x0044;
	public static final int IC_CREDIT_CARD		= 0x0045;
	public static final int IC_TIME				= 0x0046;
	public static final int IC_RUBY				= 0x0047;
	public static final int IC_PODCAST			= 0x0048;
	public static final int IC_KEY				= 0x0049;
	public static final int IC_FORCE_PUSH		= 0x004a;
	public static final int IC_SYNC				= 0x004b;
	public static final int IC_CLONE			= 0x004c;
	public static final int IC_DIFF				= 0x004d;
	public static final int IC_WATCHERS			= 0x004e;
	public static final int IC_DISCUSSION		= 0x004f;
	public static final int IC_DELETE_NOTE		= 0x0050;
	public static final int IC_REMOVE_CLOSE		= 0x0050;
	public static final int IC_REPLY			= 0x0051;
	public static final int IC_MAIL_STATUS		= 0x0052;
	public static final int IC_BLOCK			= 0x0053;
	public static final int IC_TAG_CREATE		= 0x0054;
	public static final int IC_TAG_DELETE		= 0x0055;
	public static final int IC_BRANCH_CREATE	= 0x0056;
	public static final int IC_BRANCH_DELETE	= 0x0057;
	public static final int IC_EDIT				= 0x0058;
	public static final int IC_INFO				= 0x0059;
	public static final int IC_ARR_COLLAPSED	= 0x005a;
	public static final int IC_ARR_EXPANDED		= 0x005b;
	public static final int IC_LINK				= 0x005c;
	public static final int IC_ADD				= 0x005d;
	public static final int IC_REORDER			= 0x005e;
	public static final int IC_CODE				= 0x005f;
	public static final int IC_LOCATION			= 0x0060;
	public static final int IC_U_LIST			= 0x0061;
	public static final int IC_O_LIST			= 0x0062;
	public static final int IC_QUOTEMARK		= 0x0063;
	public static final int IC_VERSION			= 0x0064;
	public static final int IC_BRIGHTNESS		= 0x0065;
	public static final int IC_FULLSCREEN		= 0x0066;
	public static final int IC_NORMALSCREEN		= 0x0067;
	public static final int IC_CALENDAR			= 0x0068;
	public static final int IC_BEER				= 0x0069;
	public static final int IC_LOCK				= 0x006a;
	public static final int IC_SECURE			= 0x006a;
	public static final int IC_ADDED			= 0x006b;
	public static final int IC_REMOVED			= 0x006c;
	public static final int IC_MODIFIED			= 0x006d;
	public static final int IC_MOVED			= 0x006e;
	public static final int IC_ADD_COMMENT		= 0x006f;
	public static final int IC_HORIZONTAL_RULE	= 0x0070;
	public static final int IC_ARR_RIGHT_MINI	= 0x0071;
	public static final int IC_JUMP_DOWN		= 0x0072;
	public static final int IC_JUMP_UP			= 0x0073;

	private
	PaintDrawable mPaintDrawable;
	private
	TextView mTextView;

	private
	float mSize = 20.0f;
	private
	int mCurrentOcticon = 0x0000;

	public
	OcticonView(Context context)
	{
		super(context);
		initialize(null);
	}

	public
	OcticonView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize(attrs);
	}

	public
	OcticonView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialize(attrs);
	}

	protected
	void initialize(AttributeSet attrs)
	{
		if (OCTICON_TYPE == null)
			OCTICON_TYPE = Typeface.createFromAsset(getContext().getAssets(), OCTICON_FONT);

		mPaintDrawable = new PaintDrawable(Color.TRANSPARENT);
		mPaintDrawable.setCornerRadius(OCTICON_CORNER_RADIUS);
		setBackgroundDrawable(mPaintDrawable);

		mTextView = new TextView(getContext());
		mTextView.setBackgroundDrawable(null);
		mTextView.setTypeface(OCTICON_TYPE);
		mTextView.setTextColor(Color.WHITE);
		LayoutParams textViewParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
		textViewParams.addRule(CENTER_IN_PARENT);
		addView(mTextView, textViewParams);

		LayoutParams layoutParams = (LayoutParams) getLayoutParams();
		if (layoutParams == null)
			layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
		setLayoutParams(layoutParams);

		if (attrs != null) {
			int[] handledAttrs = {
				android.R.attr.textSize,
				android.R.attr.textColor
			};
			final TypedArray attrArray = getContext().obtainStyledAttributes(attrs, handledAttrs);
			setGlyphSize(attrArray.getDimension(0, 24));
			mTextView.setTextColor(attrArray.getColor(1, Color.WHITE));
		}
		setId(getId());
	}

	@Override
	public
	void setId(int id)
	{
		super.setId(id);

		switch (id) {
		default:
		case NO_ID:
			break;
		case R.id.octicon_account_settings:
			setOcticon(IC_ACCOUNT_SETTINGS);
			break;
		case R.id.octicon_add:
			setOcticon(IC_ADD);
			break;
		case R.id.octicon_add_comment:
			setOcticon(IC_ADD_COMMENT);
			break;
		case R.id.octicon_added:
			setOcticon(IC_ADDED);
			break;
		case R.id.octicon_admin_tools:
			setOcticon(IC_ADMIN_TOOLS);
			break;
		case R.id.octicon_advanced_search:
			setOcticon(IC_ADVANCED_SEARCH);
			break;
		case R.id.octicon_android:
			setOcticon(IC_ANDROID);
			break;
		case R.id.octicon_apple:
			setOcticon(IC_APPLE);
			break;
		case R.id.octicon_arr_collapsed:
			setOcticon(IC_ARR_COLLAPSED);
			break;
		case R.id.octicon_arr_down:
			setOcticon(IC_ARR_DOWN);
			break;
		case R.id.octicon_arr_expanded:
			setOcticon(IC_ARR_EXPANDED);
			break;
		case R.id.octicon_arr_left:
			setOcticon(IC_ARR_LEFT);
			break;
		case R.id.octicon_arr_right:
			setOcticon(IC_ARR_RIGHT);
			break;
		case R.id.octicon_arr_right_mini:
			setOcticon(IC_ARR_RIGHT_MINI);
			break;
		case R.id.octicon_arr_up:
			setOcticon(IC_ARR_UP);
			break;
		case R.id.octicon_beer:
			setOcticon(IC_BEER);
			break;
		case R.id.octicon_blacktocat:
			setOcticon(IC_BLACKTOCAT);
			break;
		case R.id.octicon_block:
			setOcticon(IC_BLOCK);
			break;
		case R.id.octicon_branch_create:
			setOcticon(IC_BRANCH_CREATE);
			break;
		case R.id.octicon_branch_delete:
			setOcticon(IC_BRANCH_DELETE);
			break;
		case R.id.octicon_brightness:
			setOcticon(IC_BRIGHTNESS);
			break;
		case R.id.octicon_calendar:
			setOcticon(IC_CALENDAR);
			break;
		case R.id.octicon_clipboard:
			setOcticon(IC_CLIPBOARD);
			break;
		case R.id.octicon_clone:
			setOcticon(IC_CLONE);
			break;
		case R.id.octicon_code:
			setOcticon(IC_CODE);
			break;
		case R.id.octicon_code_file:
			setOcticon(IC_CODE_FILE);
			break;
		case R.id.octicon_commit:
			setOcticon(IC_COMMIT);
			break;
		case R.id.octicon_commit_comment:
			setOcticon(IC_COMMIT_COMMENT);
			break;
		case R.id.octicon_confirm:
			setOcticon(IC_CONFIRM);
			break;
		case R.id.octicon_create:
			setOcticon(IC_CREATE);
			break;
		case R.id.octicon_credit_card:
			setOcticon(IC_CREDIT_CARD);
			break;
		case R.id.octicon_delete:
			setOcticon(IC_DELETE);
			break;
		case R.id.octicon_delete_note:
			setOcticon(IC_DELETE_NOTE);
			break;
		case R.id.octicon_diff:
			setOcticon(IC_DIFF);
			break;
		case R.id.octicon_directory:
			setOcticon(IC_DIRECTORY);
			break;
		case R.id.octicon_discussion:
			setOcticon(IC_DISCUSSION);
			break;
		case R.id.octicon_download:
			setOcticon(IC_DOWNLOAD);
			break;
		case R.id.octicon_download_media:
			setOcticon(IC_DOWNLOAD_MEDIA);
			break;
		case R.id.octicon_download_pdf:
			setOcticon(IC_DOWNLOAD_PDF);
			break;
		case R.id.octicon_download_tag:
			setOcticon(IC_DOWNLOAD_TAG);
			break;
		case R.id.octicon_download_text:
			setOcticon(IC_DOWNLOAD_TEXT);
			break;
		case R.id.octicon_download_unknown:
			setOcticon(IC_DOWNLOAD_UNKNOWN);
			break;
		case R.id.octicon_download_zip:
			setOcticon(IC_DOWNLOAD_ZIP);
			break;
		case R.id.octicon_edit:
			setOcticon(IC_EDIT);
			break;
		case R.id.octicon_exclamation:
			setOcticon(IC_EXCLAMATION);
			break;
		case R.id.octicon_feed:
			setOcticon(IC_FEED);
			break;
		case R.id.octicon_follow:
			setOcticon(IC_FOLLOW);
			break;
		case R.id.octicon_force_push:
			setOcticon(IC_FORCE_PUSH);
			break;
		case R.id.octicon_fork:
			setOcticon(IC_FORK);
			break;
		case R.id.octicon_fullscreen:
			setOcticon(IC_FULLSCREEN);
			break;
		case R.id.octicon_gift:
			setOcticon(IC_GIFT);
			break;
		case R.id.octicon_gist:
			setOcticon(IC_GIST);
			break;
		case R.id.octicon_gist_private:
			setOcticon(IC_GIST_PRIVATE);
			break;
		case R.id.octicon_graph:
			setOcticon(IC_GRAPH);
			break;
		case R.id.octicon_help:
			setOcticon(IC_HELP);
			break;
		case R.id.octicon_horizontal_rule:
			setOcticon(IC_HORIZONTAL_RULE);
			break;
		case R.id.octicon_info:
			setOcticon(IC_INFO);
			break;
		case R.id.octicon_invertocat:
			setOcticon(IC_INVERTOCAT);
			break;
		case R.id.octicon_ios:
			setOcticon(IC_IOS);
			break;
		case R.id.octicon_issue_closed:
			setOcticon(IC_ISSUE_CLOSED);
			break;
		case R.id.octicon_issue_comment:
			setOcticon(IC_ISSUE_COMMENT);
			break;
		case R.id.octicon_issue_opened:
			setOcticon(IC_ISSUE_OPENED);
			break;
		case R.id.octicon_issue_reopened:
			setOcticon(IC_ISSUE_REOPENED);
			break;
		case R.id.octicon_jump_down:
			setOcticon(IC_JUMP_DOWN);
			break;
		case R.id.octicon_jump_up:
			setOcticon(IC_JUMP_UP);
			break;
		case R.id.octicon_key:
			setOcticon(IC_KEY);
			break;
		case R.id.octicon_keyboard:
			setOcticon(IC_KEYBOARD);
			break;
		case R.id.octicon_link:
			setOcticon(IC_LINK);
			break;
		case R.id.octicon_location:
			setOcticon(IC_LOCATION);
			break;
		case R.id.octicon_lock:
			setOcticon(IC_LOCK);
			break;
		case R.id.octicon_logout:
			setOcticon(IC_LOGOUT);
			break;
		case R.id.octicon_mail_status:
			setOcticon(IC_MAIL_STATUS);
			break;
		case R.id.octicon_member_added:
			setOcticon(IC_MEMBER_ADDED);
			break;
		case R.id.octicon_member_removed:
			setOcticon(IC_MEMBER_REMOVED);
			break;
		case R.id.octicon_merge:
			setOcticon(IC_MERGE);
			break;
		case R.id.octicon_modified:
			setOcticon(IC_MODIFIED);
			break;
		case R.id.octicon_moved:
			setOcticon(IC_MOVED);
			break;
		case R.id.octicon_normalscreen:
			setOcticon(IC_NORMALSCREEN);
			break;
		case R.id.octicon_notifications:
			setOcticon(IC_NOTIFICATIONS);
			break;
		case R.id.octicon_o_list:
			setOcticon(IC_O_LIST);
			break;
		case R.id.octicon_octocat:
			setOcticon(IC_OCTOCAT);
			break;
		case R.id.octicon_person:
			setOcticon(IC_PERSON);
			break;
		case R.id.octicon_pin:
			setOcticon(IC_PIN);
			break;
		case R.id.octicon_podcast:
			setOcticon(IC_PODCAST);
			break;
		case R.id.octicon_private_fork:
			setOcticon(IC_PRIVATE_FORK);
			break;
		case R.id.octicon_private_mirror:
			setOcticon(IC_PRIVATE_MIRROR);
			break;
		case R.id.octicon_private_repo:
			setOcticon(IC_PRIVATE_REPO);
			break;
		case R.id.octicon_public_fork:
			setOcticon(IC_PUBLIC_FORK);
			break;
		case R.id.octicon_public_mirror:
			setOcticon(IC_PUBLIC_MIRROR);
			break;
		case R.id.octicon_public_repo:
			setOcticon(IC_PUBLIC_REPO);
			break;
		case R.id.octicon_pull:
			setOcticon(IC_PULL);
			break;
		case R.id.octicon_pull_request:
			setOcticon(IC_PULL_REQUEST);
			break;
		case R.id.octicon_push:
			setOcticon(IC_PUSH);
			break;
		case R.id.octicon_quotemark:
			setOcticon(IC_QUOTEMARK);
			break;
		case R.id.octicon_read_note:
			setOcticon(IC_READ_NOTE);
			break;
		case R.id.octicon_readme:
			setOcticon(IC_README);
			break;
		case R.id.octicon_remove_close:
			setOcticon(IC_REMOVE_CLOSE);
			break;
		case R.id.octicon_removed:
			setOcticon(IC_REMOVED);
			break;
		case R.id.octicon_reorder:
			setOcticon(IC_REORDER);
			break;
		case R.id.octicon_reply:
			setOcticon(IC_REPLY);
			break;
		case R.id.octicon_repo_forked:
			setOcticon(IC_REPO_FORKED);
			break;
		case R.id.octicon_ruby:
			setOcticon(IC_RUBY);
			break;
		case R.id.octicon_search_input:
			setOcticon(IC_SEARCH_INPUT);
			break;
		case R.id.octicon_secure:
			setOcticon(IC_SECURE);
			break;
		case R.id.octicon_star:
			setOcticon(IC_STAR);
			break;
		case R.id.octicon_submodule:
			setOcticon(IC_SUBMODULE);
			break;
		case R.id.octicon_sync:
			setOcticon(IC_SYNC);
			break;
		case R.id.octicon_tag_create:
			setOcticon(IC_TAG_CREATE);
			break;
		case R.id.octicon_tag_delete:
			setOcticon(IC_TAG_DELETE);
			break;
		case R.id.octicon_team:
			setOcticon(IC_TEAM);
			break;
		case R.id.octicon_text_file:
			setOcticon(IC_TEXT_FILE);
			break;
		case R.id.octicon_time:
			setOcticon(IC_TIME);
			break;
		case R.id.octicon_u_list:
			setOcticon(IC_U_LIST);
			break;
		case R.id.octicon_unread_note:
			setOcticon(IC_UNREAD_NOTE);
			break;
		case R.id.octicon_unwatch:
			setOcticon(IC_UNWATCH);
			break;
		case R.id.octicon_upload:
			setOcticon(IC_UPLOAD);
			break;
		case R.id.octicon_version:
			setOcticon(IC_VERSION);
			break;
		case R.id.octicon_watchers:
			setOcticon(IC_WATCHERS);
			break;
		case R.id.octicon_watching:
			setOcticon(IC_WATCHING);
			break;
		case R.id.octicon_wiki:
			setOcticon(IC_WIKI);
			break;
		case R.id.octicon_windows:
			setOcticon(IC_WINDOWS);
			break;
		case R.id.octicon_wrench:
			setOcticon(IC_WRENCH);
			break;
		}
	}

	public
	PaintDrawable getPaintDrawable()
	{
		return mPaintDrawable;
	}

	public
	TextView getTextView()
	{
		return mTextView;
	}

	public
	OcticonView setOcticon(final int icon)
	{
		mCurrentOcticon = icon;
		if (mTextView.getPaint() != null) {
			if (mTextView.getTextSize() > 16.0f)
				mTextView.setText(
						Html.fromHtml("&#x" + Integer.toHexString(MEGA + mCurrentOcticon) + ";"));
			else
				mTextView.setText(
						Html.fromHtml("&#x" + Integer.toHexString(MINI + mCurrentOcticon) + ";"));
		}
		/*
		 * A hack to get rid of the bottom padding beneath these glyphs.
		 * setIncludeFontPadding(false) just causes a load of other problems, so this is really
		 * the only decent solution at the moment.
		 */
		mTextView.setPadding(0, 0, 0, Math.round(mTextView.getPaint().ascent() / 10.0f));
		return this;
	}

	public
	OcticonView setShowTile(final boolean showBackground)
	{
		mPaintDrawable.setVisible(showBackground, false);
		return this;
	}

	public
	OcticonView setTileColor(final int color)
	{
		mPaintDrawable.getPaint().setColor(color);
		return this;
	}

	public
	OcticonView setGlyphSize(final float size)
	{
		mTextView.setTextSize(size);
		return setOcticon(mCurrentOcticon);
	}

	public
	OcticonView setGlyphColor(final int color)
	{
		mTextView.setTextColor(color);
		return this;
	}

	public
	Bitmap toBitmap(final int sizing)
	{
		if (!(getMeasuredHeight() > 0 && getMeasuredWidth() > 0)) {
			measure(MeasureSpec.makeMeasureSpec(sizing, EXACTLY),
					MeasureSpec.makeMeasureSpec(sizing, EXACTLY));
			layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
		}
		final Bitmap bmp = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
											   Bitmap.Config.ARGB_8888);
		final Canvas cvs = new Canvas(bmp);
		draw(cvs);
		return bmp;
	}

	public
	Bitmap toBitmap()
	{
		return toBitmap(WRAP_CONTENT);
	}

	public
	BitmapDrawable toDrawable(final int sizing)
	{
		return new BitmapDrawable(getResources(), toBitmap(sizing));
	}

	public
	BitmapDrawable toDrawable()
	{
		return toDrawable(WRAP_CONTENT);
	}
}
