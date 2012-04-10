/*
 * Copyright (c) 2012 Eddie Ringle
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following
 * disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.idlesoft.android.apps.github.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import net.idlesoft.android.apps.github.R;

public abstract
class ShellActivity<F extends Fragment> extends BaseActivity
{
	protected
	Class<F> mFragmentClass;

	@Override
	protected
	void onCreate(Bundle icicle)
	{
		super.onCreate(icicle, R.layout.shell_layout);

		/* If we're multi-paned all of a sudden, bomb out */
		boolean isMultiPaned = isMultiPane();
		if (isMultiPaned) {
			finish();
			return;
		}

		if (icicle == null) {
			/* Plug in the associated fragment by default */
			try {
				Fragment fragment = (Fragment) mFragmentClass.newInstance();
				fragment.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.fragment_container, fragment).commit();
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalStateException();
			}
		}
	}
}
