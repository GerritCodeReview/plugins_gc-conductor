// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.ericsson.gerrit.plugins.gcconductor.executor;

import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CancellableProgressMonitor extends BatchingProgressMonitor {
  private boolean cancelled = false;
  private static final Logger log = LoggerFactory.getLogger(CancellableProgressMonitor.class);

  /** {@inheritDoc} */
  @Override
  protected void onUpdate(String taskName, int workCurr) {
    StringBuilder s = new StringBuilder();
    format(s, taskName, workCurr);
    send(s);
  }

  /** {@inheritDoc} */
  @Override
  protected void onEndTask(String taskName, int workCurr) {
    StringBuilder s = new StringBuilder();
    format(s, taskName, workCurr);
    s.append("\n"); // $NON-NLS-1$
    send(s);
  }

  private void format(StringBuilder s, String taskName, int workCurr) {
    s.append("\r"); // $NON-NLS-1$
    s.append(taskName);
    s.append(": "); // $NON-NLS-1$
    while (s.length() < 25) s.append(' ');
    s.append(workCurr);
  }

  /** {@inheritDoc} */
  @Override
  protected void onUpdate(String taskName, int cmp, int totalWork, int pcnt) {
    StringBuilder s = new StringBuilder();
    format(s, taskName, cmp, totalWork, pcnt);
    send(s);
  }

  /** {@inheritDoc} */
  @Override
  protected void onEndTask(String taskName, int cmp, int totalWork, int pcnt) {
    StringBuilder s = new StringBuilder();
    format(s, taskName, cmp, totalWork, pcnt);
    s.append("\n"); // $NON-NLS-1$
    send(s);
  }

  private void format(StringBuilder s, String taskName, int cmp, int totalWork, int pcnt) {
    s.append("\r"); // $NON-NLS-1$
    s.append(taskName);
    s.append(": "); // $NON-NLS-1$
    while (s.length() < 25) s.append(' ');

    String endStr = String.valueOf(totalWork);
    String curStr = String.valueOf(cmp);
    while (curStr.length() < endStr.length()) curStr = " " + curStr; // $NON-NLS-1$
    if (pcnt < 100) s.append(' ');
    if (pcnt < 10) s.append(' ');
    s.append(pcnt);
    s.append("% ("); // $NON-NLS-1$
    s.append(curStr);
    s.append("/"); // $NON-NLS-1$
    s.append(endStr);
    s.append(")"); // $NON-NLS-1$
  }

  private void send(StringBuilder s) {
    log.debug("{}, s.toString()");
  }

  void cancel() {
    cancelled = true;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }
}
