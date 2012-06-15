/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.fragments;

import android.accounts.AccountsException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.petebevin.markdown.MarkdownProcessor;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.ui.adapters.InfoListAdapter;
import net.idlesoft.android.apps.github.ui.adapters.IssueCommentListAdapter;
import net.idlesoft.android.apps.github.ui.widgets.IdleList;
import net.idlesoft.android.apps.github.ui.widgets.OcticonView;
import net.idlesoft.android.apps.github.utils.IssueUtils;
import net.idlesoft.android.apps.github.utils.RequestCache;
import net.idlesoft.android.apps.github.utils.StringUtils;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.util.ArrayList;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_ISSUE;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_USER;
import static net.idlesoft.android.apps.github.utils.StringUtils.isStringEmpty;

public
class IssueFragment extends UIFragment<IssueFragment.IssueDataFragment>
{
	public static
	class IssueDataFragment extends DataFragment
	{
		ListView list;
		IssueCommentListAdapter commentAdapter;
		ArrayList<Comment> issueComments;
		Issue targetIssue;
		View issueDetailsView;
		View issueContentsView;
	}

	private
	ProgressBar mProgress;
	private
	LinearLayout mContent;
	private
	EditText mCommentText;
	private
	ImageButton mCommentButton;

	public
	IssueFragment()
	{
		super(IssueDataFragment.class);
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			getBaseActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

		View v = inflater.inflate(R.layout.single_issue, container, false);

		if (v != null) {
			mProgress = (ProgressBar) v.findViewById(R.id.progress);
			mContent = (LinearLayout) v.findViewById(R.id.content);
			mCommentText = (EditText) v.findViewById(R.id.et_issue_comment);
			mCommentButton = (ImageButton) v.findViewById(R.id.ib_issue_comment);
		}

		return v;
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (getView() != null) {
			mDataFragment.list = (ListView) getView().findViewById(R.id.lv_issue);
		}

		final Bundle args = getArguments();
		final String issueJson;
		if (args != null) {
			issueJson = args.getString(ARG_TARGET_ISSUE);
			if (issueJson != null) {
				mDataFragment.targetIssue = GsonUtils.fromJson(issueJson, Issue.class);
			}
		}

		mDataFragment.commentAdapter = new IssueCommentListAdapter(getBaseActivity());
		fetchData(false);
	}

	public
	void fetchData(final boolean freshen)
	{
		if (mDataFragment.issueComments != null && !freshen) {
			mDataFragment.commentAdapter.fillWithItems(mDataFragment.issueComments);
			mDataFragment.commentAdapter.notifyDataSetChanged();
			mContent.setVisibility(View.VISIBLE);
			buildUI();
		} else {
			final DataFragment.DataTask.DataTaskRunnable repositoryRunnable =
					new DataFragment.DataTask.DataTaskRunnable()
					{
						@Override
						public
						void runTask() throws InterruptedException
						{
							final String repoOwner, repoName, htmlUrl;
							htmlUrl = mDataFragment.targetIssue.getHtmlUrl().replaceAll("/{2,}", "/");
							repoOwner = htmlUrl.split("/")[2];
							repoName = htmlUrl.split("/")[3];

							try {
								final IssueService is = new IssueService(getBaseActivity()
																				 .getGHClient());
								if (mDataFragment.targetIssue.getCreatedAt() == null) {
									mDataFragment.targetIssue =
											is.getIssue(repoOwner,
														repoName,
														mDataFragment.targetIssue.getNumber());
								}
								mDataFragment.issueComments = new ArrayList<Comment>(
										is.getComments(repoOwner,
													   repoName,
													   mDataFragment.targetIssue.getNumber()));
								mDataFragment.commentAdapter.fillWithItems(mDataFragment.issueComments);
							} catch (IOException e) {
								e.printStackTrace();
							} catch (AccountsException e) {
								e.printStackTrace();
							}
						}
					};

			final DataFragment.DataTask.DataTaskCallbacks repositoryCallbacks =
					new DataFragment.DataTask.DataTaskCallbacks()
					{
						@Override
						public
						void onTaskStart()
						{
							mContent.setVisibility(View.GONE);
							mProgress.setVisibility(View.VISIBLE);
						}

						@Override
						public
						void onTaskCancelled()
						{
						}

						@Override
						public
						void onTaskComplete()
						{
							buildUI();
							mProgress.setVisibility(View.GONE);
							mContent.setVisibility(View.VISIBLE);
						}
					};

			mDataFragment.executeNewTask(repositoryRunnable, repositoryCallbacks);
		}
	}

	public
	void buildUI()
	{
		TextView issueContents = new TextView(getBaseActivity());
		issueContents.setTextSize(12.0f);
		issueContents.setMovementMethod(new LinkMovementMethod());
		final DisplayMetrics dm = getResources().getDisplayMetrics();
		issueContents.setPadding((int)TypedValue.applyDimension(COMPLEX_UNIT_DIP, 10.0f, dm),
								 (int)TypedValue.applyDimension(COMPLEX_UNIT_DIP, 10.0f, dm),
								 (int)TypedValue.applyDimension(COMPLEX_UNIT_DIP, 10.0f, dm),
								 (int)TypedValue.applyDimension(COMPLEX_UNIT_DIP, 10.0f, dm));
		if (!StringUtils.isStringEmpty(mDataFragment.targetIssue.getBody())) {
			final String rawBody = mDataFragment.targetIssue.getBody();
			final MarkdownProcessor processor = new MarkdownProcessor();
			final String processedBody = processor.markdown(rawBody);
			issueContents.setText(StringUtils.trimTrailingWhitespace(Html.fromHtml(processedBody)));
		} else {
			issueContents.setText(getString(R.string.issue_empty_description));
		}

		if (mDataFragment.issueDetailsView != null)
			mDataFragment.list.removeHeaderView(mDataFragment.issueDetailsView);
		if (mDataFragment.issueContentsView != null)
			mDataFragment.list.removeHeaderView(mDataFragment.issueContentsView);

		mDataFragment.issueDetailsView = IssueUtils.viewFromIssue(getBaseActivity(),
																  mDataFragment.targetIssue);
		mDataFragment.issueContentsView = issueContents;

		mDataFragment.list.addHeaderView(mDataFragment.issueDetailsView);
		mDataFragment.list.addHeaderView(mDataFragment.issueContentsView);

		mDataFragment.list.setAdapter(mDataFragment.commentAdapter);
		/* Allow items within list items to be clickable */
		mDataFragment.list.setItemsCanFocus(true);
	}

	@Override
	public
	void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		menu.findItem(R.id.actionbar_action_refresh).setVisible(true);
	}

	@Override
	public
	boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.actionbar_action_refresh:
				fetchData(true);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
