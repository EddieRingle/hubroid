/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.fragments;

import android.accounts.AccountsException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.actionbarsherlock.app.ActionBar;
import net.idlesoft.android.apps.github.R;
import net.idlesoft.android.apps.github.utils.DataTask;
import net.idlesoft.android.apps.github.utils.StringUtils;
import net.idlesoft.android.apps.github.utils.ToastUtil;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static net.idlesoft.android.apps.github.HubroidConstants.ARG_TARGET_REPO;

public
class NewIssueFragment extends UIFragment<NewIssueFragment.NewIssueDataFragment>
{

	public static
	class NewIssueDataFragment extends DataFragment
	{
		public
		Repository targetRepository;
	}

	private
	ProgressBar mProgress;
	private
	EditText mIssueTitle;
	private
	EditText mIssueBody;
	private
	Button mSubmitButton;

	public
	NewIssueFragment()
	{
		super(NewIssueDataFragment.class);
	}

	@Override
	public
	View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			getBaseActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

		View v = inflater.inflate(R.layout.new_issue, container, false);

		if (v != null) {
			mProgress = (ProgressBar) v.findViewById(R.id.pb_issue_submit_progress);
			mIssueTitle = (EditText) v.findViewById(R.id.et_issue_title);
			mIssueBody = (EditText) v.findViewById(R.id.et_issue_body);
			mSubmitButton = (Button) v.findViewById(R.id.btn_issue_submit);
		}

		return v;
	}

	@Override
	public
	void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		final Bundle args = getArguments();
		final String repoJson;
		if (args != null) {
			repoJson = args.getString(ARG_TARGET_REPO);
			if (repoJson != null) {
				mDataFragment.targetRepository = GsonUtils.fromJson(repoJson, Repository.class);
			}
		}

		mSubmitButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public
			void onClick(View view)
			{
				final String titleText = mIssueTitle.getText().toString();
				final String bodyText = mIssueBody.getText().toString();

				if (StringUtils.isStringEmpty(titleText))
					return;

				DataTask.Executable issueExecutable = new DataTask.Executable()
				{
					@Override
					public
					void onTaskStart()
					{
						mSubmitButton.setVisibility(GONE);
						mProgress.setVisibility(VISIBLE);
					}

					@Override
					public
					void onTaskComplete()
					{
						getBaseActivity().setRefreshPrevious(true);
						getBaseActivity().onBackPressed();
					}

					@Override
					public
					void runTask() throws InterruptedException
					{
						final String repoOwner, repoName, title, body;
						repoOwner = mDataFragment.targetRepository.getOwner().getLogin();
						repoName = mDataFragment.targetRepository.getName();

						title = titleText;
						/* Append "Sent via Hubroid" to the issue */
						body = bodyText + "\n\n_Sent via Hubroid_";

						try {
							final IssueService is =
									new IssueService(getBaseActivity().getGHClient());
							final Issue newIssue = new Issue();
							newIssue.setTitle(title);
							newIssue.setBody(body);
							final Issue resultIssue = is.createIssue(repoOwner,
																	 repoName,
																	 newIssue);
							if (resultIssue == null) {
								ToastUtil.toastOnUiThread(getBaseActivity(), "Something went wrong.");
							}
						} catch (AccountsException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				mDataFragment.executeNewTask(issueExecutable);
			}
		});
	}

	@Override
	public
	void onCreateActionBar(ActionBar bar)
	{
		super.onCreateActionBar(bar);

		bar.setTitle(R.string.actionbar_action_add_issue);
	}
}
