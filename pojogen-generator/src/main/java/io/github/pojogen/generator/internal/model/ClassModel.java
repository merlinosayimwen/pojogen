/*
 * Copyright 2018 Merlin Osayimwen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pojogen.generator.internal.model;

import static java.text.MessageFormat.format;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.github.pojogen.generator.internal.GenerationContext;
import io.github.pojogen.generator.internal.GenerationStep;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class ClassModel implements GenerationStep {

  private final String className;
  private final Collection<? extends GenerationStep> members;

  private ClassModel(final String className, final Collection<? extends GenerationStep> members) {
    this.className = className;
    this.members = ImmutableList.copyOf(members);
  }

  @Override
  public void writeToContext(final GenerationContext context) {
    final Consumer<GenerationStep> writeStepToContextFunction =
        step -> step.writeToContext(context);

    // TODO(merlinosayimwen): Make this a little bit more configurable.
    context.getBuffer().write(format("public final class {0} '{'", this.className));
    context.getDepth().incrementByOne();
    context.getBuffer().writeLine();

    // Writes every member to the context.
    this.members.forEach(writeStepToContextFunction);

    context.getDepth().decrementByOne();
    context.getBuffer().writeLine();
    context.getBuffer().write("}");
  }

  public String getClassName() {
    return this.className;
  }

  public Stream<? extends GenerationStep> getMembers() {
    return this.members.stream();
  }

  public static ClassModel create(
      final String className, final Collection<? extends GenerationStep> members) {
    Preconditions.checkNotNull(className);
    Preconditions.checkNotNull(members);

    return new ClassModel(className, members);
  }
}