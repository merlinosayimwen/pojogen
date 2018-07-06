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

package io.github.pojogen.generator;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.github.pojogen.generator.util.RepresentationSnapshot;
import io.github.pojogen.struct.util.ObjectChecks;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Lists settings and preferences or a {@code pojo generation}.
 *
 * <p>The {@code PojoGenerator} allows to enter some preferences on how things should be generated
 * internally. Those are either in the form of a {@code GenerationFlag} which is providing
 * fundamental configurability or {@code custom properties} which are there for finer preferences.
 *
 * @since 1.0
 * @see RepresentationSnapshot
 * @see GenerationFlag
 * @see PojoGenerator
 */
public final class GenerationProfile {

  /** Collection of flags set for the generation. */
  private final Collection<GenerationFlag> flags;

  private final Map<String, String> properties;

  private RepresentationSnapshot representation;

  /** No-args constructor of the GenerationProfile class. */
  private GenerationProfile() {
    this(Collections.emptyList());
  }

  private GenerationProfile(final Iterable<GenerationFlag> flags) {
    this(flags, new HashMap<>());
  }

  /**
   * Parameterized constructor that creates a GenerationProfile with zero ore more flags.
   *
   * @param flags Zero ore more flags set for the generation.
   * @param properties Map with custom properties for the generation.
   */
  private GenerationProfile(
      final Iterable<GenerationFlag> flags, final Map<String, String> properties) {

    this.flags = Sets.newEnumSet(flags, GenerationFlag.class);
    this.properties = ImmutableMap.copyOf(properties);
  }

  /**
   * Gets whether the given {@code flag} is contained in the profiles flags.
   *
   * @param flag Flag who's presence in the profiles flags is to be tested.
   * @return Whether the {@code flag} is contained in the profiles flags.
   */
  public boolean hasFlag(final GenerationFlag flag) {
    return this.flags.contains(flag);
  }

  /**
   * Gets a stream of all flags configured in the profile.
   *
   * <p>Since the profiles flags may not be modified after creation, using a stream to analysing
   * them is good practice and safer.
   *
   * @return Stream of all flags configured in the profile.
   */
  public Stream<GenerationFlag> streamFlags() {
    return this.flags.stream();
  }

  /**
   * Gets a iterable of all flags configured in the profile.
   *
   * @return Iterable of all flags configured in the profile.
   */
  public Iterable<GenerationFlag> getFlags() {
    return ImmutableSet.copyOf(this.flags);
  }

  /**
   * Gets the named property.
   *
   * @param name Name of the property that will be returned.
   * @return Optional property with the given {@code name}.
   */
  public Optional<String> getProperty(final String name) {
    Preconditions.checkNotNull(name);

    return Optional.ofNullable(this.properties.get(name));
  }

  /**
   * Gets the properties of the profile.
   *
   * @return Map of defined properties for the generation.
   */
  public Map<String, String> getProperties() {
    return ImmutableMap.copyOf(properties);
  }

  @Override
  public boolean equals(final Object checkTarget) {
    return ObjectChecks.equalsDefinitely(this, checkTarget)
        .orElseGet(() -> deepEquals(checkTarget));
  }

  private boolean deepEquals(final Object checkTarget) {
    if (!(checkTarget instanceof GenerationProfile)) {
      return false;
    }

    final GenerationProfile other = (GenerationProfile) checkTarget;

    return Arrays.deepEquals(this.flags.toArray(), other.flags.toArray())
        && Arrays.deepEquals(properties.entrySet().toArray(), other.properties.entrySet().toArray())
        && Arrays.deepEquals(properties.values().toArray(), other.properties.values().toArray());
  }

  @Override
  public int hashCode() {
    return this.representation.getHashCode();
  }

  private int calculateHashCodeOnce() {
    final int flagsHashed = Objects.hash(this.flags.toArray());
    final int hashedProperties =
        Objects.hash(properties.values().toArray()) + Objects.hash(properties.keySet().toArray());

    return flagsHashed + hashedProperties;
  }

  @Override
  public String toString() {
    return this.representation.getStringRepresentation();
  }

  private String generateStringRepresentationOnce() {
    final String flagsRepresentation = Joiner.on(',').join(this.flags);
    final String propertiesRepresentation =
        Joiner.on(",").withKeyValueSeparator(":").join(this.properties);

    return MoreObjects.toStringHelper(this)
        .add("flags", "{" + flagsRepresentation + "}")
        .add("properties", "{" + propertiesRepresentation + "}")
        .toString();
  }

  /** Builder that allows easy creation of a GenerationProfile. */
  public static final class Builder {

    private Collection<GenerationFlag> flags;

    private Builder() {
      this.flags = Sets.newHashSet();
    }

    public Builder addFlag(final GenerationFlag flag) {
      this.flags.add(flag);
      return this;
    }

    public GenerationProfile create() {
      return GenerationProfile.create(this.flags);
    }

    public static Builder newBuilder() {
      return new Builder();
    }
  }

  /**
   * Creates a GenerationProfile without any flags given.
   *
   * @return Newly created plain GenerationProfile.
   */
  public static GenerationProfile create() {
    return GenerationProfile.create(Collections.emptyList());
  }

  /**
   * Creates a GenerationProfile from the given {@code flags}.
   *
   * @param flags Flags chosen for the generation.
   * @return Newly created profile with the given flags.
   */
  public static GenerationProfile create(final Iterable<GenerationFlag> flags) {
    return GenerationProfile.create(flags, Collections.emptyMap());
  }

  public static GenerationProfile create(
      final Iterable<GenerationFlag> flags, final Map<String, String> properties) {

    Preconditions.checkNotNull(flags);
    Preconditions.checkNotNull(properties);

    final GenerationProfile profile = new GenerationProfile(flags, properties);
    profile.representation =
        RepresentationSnapshot.create(
            profile::calculateHashCodeOnce, profile::generateStringRepresentationOnce);

    return profile;
  }
}
