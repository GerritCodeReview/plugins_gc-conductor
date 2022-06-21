// Copyright (C) 2022 The Android Open Source Project
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

package com.ericsson.gerrit.plugins.gcconductor.command;

import static com.google.gerrit.pgm.init.api.InitUtil.die;

import com.google.gerrit.entities.Project;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.git.LocalDiskRepositoryManager;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.sshd.BaseCommand.UnloggedFailure;
import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jgit.lib.Constants;

public class GcUtils {
  private GcUtils() {}

  static Path resolvePath(
      GitRepositoryManager gitRepositoryManager, ProjectCache projectCache, String repository)
      throws UnloggedFailure {
    if (!(gitRepositoryManager instanceof LocalDiskRepositoryManager)) {
      throw die("Unable to resolve path to " + repository);
    }
    String projectName = extractFrom(repository);
    Project.NameKey nameKey = Project.nameKey(projectName);
    if (projectCache.get(nameKey) == null) {
      throw die(String.format("Repository %s not found", repository));
    }
    LocalDiskRepositoryManager localDiskRepositoryManager =
        (LocalDiskRepositoryManager) gitRepositoryManager;
    try {
      return localDiskRepositoryManager
          .getBasePath(nameKey)
          .resolve(projectName.concat(Constants.DOT_GIT_EXT))
          .toRealPath();
    } catch (IOException e) {
      throw die(e.toString());
    }
  }

  static String extractFrom(String path) {
    String name = path;
    if (name.startsWith("/")) {
      name = name.substring(1);
    }
    if (name.endsWith("/")) {
      name = name.substring(0, name.length() - 1);
    }
    if (name.endsWith(Constants.DOT_GIT_EXT)) {
      name = name.substring(0, name.indexOf(Constants.DOT_GIT_EXT));
    }
    return name;
  }
}
