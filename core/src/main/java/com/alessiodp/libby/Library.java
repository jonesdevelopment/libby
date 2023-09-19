package com.alessiodp.libby;

import com.alessiodp.libby.transitive.ExcludedDependency;
import com.alessiodp.libby.relocation.Relocation;

import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * An immutable representation of a Maven artifact that can be downloaded,
 * relocated and then loaded into a plugin's classpath at runtime.
 *
 * @see #builder()
 */
public class Library {
    /**
     * Direct download URLs for this library
     */
    private final Collection<String> urls;

    /**
     * Repository URLs for this library
     */
    private final Collection<String> repositories;

    /**
     * Maven group ID
     */
    private final String groupId;

    /**
     * Maven artifact ID
     */
    private final String artifactId;

    /**
     * Artifact version
     */
    private final String version;

    /**
     * Artifact classifier
     */
    private final String classifier;

    /**
     * Binary SHA-256 checksum for this library's jar file
     */
    private final byte[] checksum;

    /**
     * Jar relocations to apply
     */
    private final Collection<Relocation> relocations;

    /**
     * Relative Maven path to this library's artifact
     */
    private final String path;

    /**
     * Relative partial Maven path to this library
     */
    private final String partialPath;

    /**
     * Relative path to this library's relocated jar
     */
    private final String relocatedPath;

    /**
     * Should this library be loaded in an isolated class loader?
     */
    private final boolean isolatedLoad;
    
    /**
     * The isolated loader id for this library
     */
    private final String loaderId;

    /**
     * Should transitive dependencies be resolved for this library?
     */
    private final boolean resolveTransitiveDependencies;

    /**
     * Transitive dependencies that would be excluded on transitive resolvement
     */
    private final Collection<ExcludedDependency> excludedTransitiveDependencies;

    /**
     * Creates a new library.
     *
     * @param urls         direct download URLs
     * @param id           library ID
     * @param groupId      Maven group ID, any {@code "{}"} is replaced with a {@code "."}
     * @param artifactId   Maven artifact ID, any {@code "{}"} is replaced with a {@code "."}
     * @param version      artifact version
     * @param classifier   artifact classifier or null
     * @param checksum     binary SHA-256 checksum or null
     * @param relocations  jar relocations or null
     * @param isolatedLoad isolated load for this library
     * @param loaderId     the loader ID for this library
     * @param resolveTransitiveDependencies transitive dependencies resolvement for this library
     * @param excludedTransitiveDependencies excluded transitive dependencies or null
     */
    private Library(Collection<String> urls,
                    String groupId,
                    String artifactId,
                    String version,
                    String classifier,
                    byte[] checksum,
                    Collection<Relocation> relocations,
                    boolean isolatedLoad,
                    String loaderId,
                    boolean resolveTransitiveDependencies,
                    Collection<ExcludedDependency> excludedTransitiveDependencies) {

        this(urls, null, groupId, artifactId, version, classifier, checksum, relocations, isolatedLoad, loaderId, resolveTransitiveDependencies, excludedTransitiveDependencies);
    }

    /**
     * Creates a new library.
     *
     * @param urls         direct download URLs
     * @param repositories repository URLs
     * @param groupId      Maven group ID, any {@code "{}"} is replaced with a {@code "."}
     * @param artifactId   Maven artifact ID, any {@code "{}"} is replaced with a {@code "."}
     * @param version      artifact version
     * @param classifier   artifact classifier or null
     * @param checksum     binary SHA-256 checksum or null
     * @param relocations  jar relocations or null
     * @param isolatedLoad isolated load for this library
     * @param loaderId     the loader ID for this library
     * @param resolveTransitiveDependencies transitive dependencies resolvement for this library
     * @param excludedTransitiveDependencies excluded transitive dependencies or null
     */
    private Library(Collection<String> urls,
                    Collection<String> repositories,
                    String groupId,
                    String artifactId,
                    String version,
                    String classifier,
                    byte[] checksum,
                    Collection<Relocation> relocations,
                    boolean isolatedLoad,
                    String loaderId,
                    boolean resolveTransitiveDependencies,
                    Collection<ExcludedDependency> excludedTransitiveDependencies) {

        this.urls = urls != null ? Collections.unmodifiableList(new LinkedList<>(urls)) : Collections.emptyList();
        this.groupId = requireNonNull(groupId, "groupId").replace("{}", ".");
        this.artifactId = requireNonNull(artifactId, "artifactId").replace("{}", ".");
        this.version = requireNonNull(version, "version");
        this.classifier = classifier;
        this.checksum = checksum;
        this.relocations = relocations != null ? Collections.unmodifiableList(new LinkedList<>(relocations)) : Collections.emptyList();

        this.partialPath = this.groupId.replace('.', '/') + '/' + artifactId + '/' + version + '/';
        String path = this.partialPath + artifactId + '-' + version;
        if (hasClassifier()) {
            path += '-' + classifier;
        }

        this.path = path + ".jar";

        this.repositories = repositories != null ? Collections.unmodifiableList(new LinkedList<>(repositories)) : Collections.emptyList();
        relocatedPath = hasRelocations() ? path + "-relocated.jar" : null;
        this.isolatedLoad = isolatedLoad;
        this.loaderId = loaderId;
        this.resolveTransitiveDependencies = resolveTransitiveDependencies;
        this.excludedTransitiveDependencies = excludedTransitiveDependencies != null ? Collections.unmodifiableList(new LinkedList<>(excludedTransitiveDependencies)) : Collections.emptyList();
    }

    /**
     * Gets the direct download URLs for this library.
     *
     * @return direct download URLs
     */
    public Collection<String> getUrls() {
        return urls;
    }

    /**
     * Gets the repositories URLs for this library.
     *
     * @return repositories URLs
     */
    public Collection<String> getRepositories() {
        return repositories;
    }

    /**
     * Gets the Maven group ID for this library.
     *
     * @return Maven group ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Gets the Maven artifact ID for this library.
     *
     * @return Maven artifact ID
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Gets the artifact version for this library.
     *
     * @return artifact version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the artifact classifier for this library.
     *
     * @return artifact classifier or null
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Gets whether this library has an artifact classifier.
     *
     * @return true if library has classifier, false otherwise
     */
    public boolean hasClassifier() {
        return classifier != null;
    }

    /**
     * Gets the binary SHA-256 checksum of this library's jar file.
     *
     * @return checksum or null
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * Gets whether this library has a checksum.
     *
     * @return true if library has checksum, false otherwise
     */
    public boolean hasChecksum() {
        return checksum != null;
    }

    /**
     * Gets the jar relocations to apply to this library.
     *
     * @return jar relocations to apply
     */
    public Collection<Relocation> getRelocations() {
        return relocations;
    }

    /**
     * Gets whether this library has any jar relocations.
     *
     * @return true if library has relocations, false otherwise
     */
    public boolean hasRelocations() {
        return !relocations.isEmpty();
    }

    /**
     * Gets the relative Maven path to this library's artifact.
     *
     * @return relative Maven path for this library
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the relative partial Maven path to this library.
     *
     * @return relative partial Maven path for this library
     */
    public String getPartialPath() {
        return partialPath;
    }

    /**
     * Gets the relative path to this library's relocated jar.
     *
     * @return path to relocated artifact or null if has no relocations
     */
    public String getRelocatedPath() {
        return relocatedPath;
    }

    /**
     * Is the library loaded isolated?
     *
     * @return true if the library is loaded isolated
     */
    public boolean isIsolatedLoad() {
        return isolatedLoad;
    }
    
    /**
     * Get the isolated loader ID
     *
     * @return the loader ID
     */
    public String getLoaderId() {
        return loaderId;
    }
    
    /**
     * Whether the library is a snapshot.
     *
     * @return whether the library is a snapshot.
     */
    public boolean isSnapshot() {
        return version.endsWith("-SNAPSHOT");
    }

    /**
     * Should transitive dependencies of this resolved
     *
     * @return true if the transitive dependencies of this library would be resolved
     */
    public boolean resolveTransitiveDependencies() {
        return resolveTransitiveDependencies;
    }

    /**
     * Gets the excluded dependencies during transitive dependencies resolution.
     *
     * @return The dependencies excluded during transitive dependencies resolution.
     */
    public Collection<ExcludedDependency> getExcludedTransitiveDependencies() {
        return excludedTransitiveDependencies;
    }

    /**
     * Gets a concise, human-readable string representation of this library.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        String name = groupId + ':' + artifactId + ':' + version;
        if (hasClassifier()) {
            name += ':' + classifier;
        }

        return name;
    }

    /**
     * Creates a new library builder.
     *
     * @return new library builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Due to the constructor complexity of an immutable {@link Library},
     * instead this fluent builder is used to configure and then construct
     * a new library.
     */
    public static class Builder {
        /**
         * Direct download URLs for this library
         */
        private final Collection<String> urls = new LinkedList<>();

        /**
         * Repository URLs for this library
         */
        private final Collection<String> repositories = new LinkedList<>();

        /**
         * Maven group ID
         */
        private String groupId;

        /**
         * Maven artifact ID
         */
        private String artifactId;

        /**
         * Artifact version
         */
        private String version;

        /**
         * Artifact classifier
         */
        private String classifier;

        /**
         * Binary SHA-256 checksum for this library's jar file
         */
        private byte[] checksum;

        /**
         * Isolated load
         */
        private boolean isolatedLoad;
        
        /**
         * Loader ID
         */
        private String loaderId;

        /**
         * Jar relocations to apply
         */
        private final Collection<Relocation> relocations = new LinkedList<>();

        /**
         * Resolve transitive dependencies
         */
        private boolean resolveTransitiveDependencies;

        /**
         * Resolve transitive dependencies exclusions
         */
        private final Collection<ExcludedDependency> excludedTransitiveDependencies = new LinkedList<>();

        /**
         * Adds a direct download URL for this library.
         *
         * @param url direct download URL
         * @return this builder
         */
        public Builder url(String url) {
            urls.add(requireNonNull(url, "url"));
            return this;
        }

        /**
         * Adds a repository URL for this library.
         * <p>Most common repositories can be found in {@link Repositories} class as constants.
         * <p>Note that repositories should be preferably added to the {@link LibraryManager} via {@link LibraryManager#addRepository(String)}.
         *
         * @param url repository URL
         * @return this builder
         */
        public Builder repository(String url) {
            repositories.add(requireNonNull(url, "repository").endsWith("/") ? url : url + '/');
            return this;
        }

        /**
         * Sets the Maven group ID for this library.
         * <p>
         * To avoid issues with shading and relocation, any {@code "{}"} inside the provided groupId string
         * is replaced with a {@code "."}.
         *
         * @param groupId Maven group ID
         * @return this builder
         */
        public Builder groupId(String groupId) {
            this.groupId = requireNonNull(groupId, "groupId");
            return this;
        }

        /**
         * Sets the Maven artifact ID for this library.
         * <p>
         * To avoid issues with shading and relocation, any {@code "{}"} inside the provided artifactId string
         * is replaced with a {@code "."}.
         *
         * @param artifactId Maven artifact ID
         * @return this builder
         */
        public Builder artifactId(String artifactId) {
            this.artifactId = requireNonNull(artifactId, "artifactId");
            return this;
        }

        /**
         * Sets the artifact version for this library.
         *
         * @param version artifact version
         * @return this builder
         */
        public Builder version(String version) {
            this.version = requireNonNull(version, "version");
            return this;
        }

        /**
         * Sets the artifact classifier for this library.
         *
         * @param classifier artifact classifier
         * @return this builder
         */
        public Builder classifier(String classifier) {
            this.classifier = requireNonNull(classifier, "classifier");
            return this;
        }

        /**
         * Sets the binary SHA-256 checksum for this library.
         *
         * @param checksum binary SHA-256 checksum
         * @return this builder
         */
        public Builder checksum(byte[] checksum) {
            this.checksum = requireNonNull(checksum, "checksum");
            return this;
        }

        /**
         * Sets the Base64-encoded SHA-256 checksum for this library.
         *
         * @param checksum Base64-encoded SHA-256 checksum
         * @return this builder
         */
        public Builder checksum(String checksum) {
            return checksum(Base64.getDecoder().decode(requireNonNull(checksum, "checksum")));
        }

        /**
         * Sets the isolated load for this library.
         *
         * @param isolatedLoad the isolated load boolean
         * @return this builder
         */
        public Builder isolatedLoad(boolean isolatedLoad) {
            this.isolatedLoad = isolatedLoad;
            return this;
        }
        
        /**
         * Sets the loader ID for this library.
         *
         * @param loaderId the ID
         * @return this builder
         */
        public Builder loaderId(String loaderId) {
            this.loaderId = loaderId;
            return this;
        }

        /**
         * Adds a jar relocation to apply to this library.
         *
         * @param relocation jar relocation to apply
         * @return this builder
         */
        public Builder relocate(Relocation relocation) {
            relocations.add(requireNonNull(relocation, "relocation"));
            return this;
        }

        /**
         * Adds a jar relocation to apply to this library.
         *
         * @param pattern          search pattern
         * @param relocatedPattern replacement pattern
         * @return this builder
         */
        public Builder relocate(String pattern, String relocatedPattern) {
            return relocate(new Relocation(pattern, relocatedPattern));
        }

        /**
         * Sets the transitive dependency resolvement for this library.
         *
         * @param resolveTransitiveDependencies the transitive dependency resolvement
         * @return this builder
         * @see #excludeTransitiveDependency(ExcludedDependency)
         */
        public Builder resolveTransitiveDependencies(boolean resolveTransitiveDependencies) {
            this.resolveTransitiveDependencies = resolveTransitiveDependencies;
            return this;
        }

        /**
         * Excludes transitive dependency for this library.
         *
         * @param excludedDependency Excluded transitive dependency
         * @return this builder
         * @see #resolveTransitiveDependencies(boolean)
         */
        public Builder excludeTransitiveDependency(ExcludedDependency excludedDependency) {
            excludedTransitiveDependencies.add(excludedDependency);
            return this;
        }

        /**
         * Excludes transitive dependency for this library.
         *
         * @param groupId Excluded transitive dependency group ID
         * @param artifactId Excluded transitive dependency artifact ID
         * @return this builder
         * @see #excludeTransitiveDependency(ExcludedDependency)
         */
        public Builder excludeTransitiveDependency(String groupId, String artifactId) {
            return excludeTransitiveDependency(new ExcludedDependency(groupId, artifactId));
        }

        /**
         * Creates a new library using this builder's configuration.
         *
         * @return new library
         */
        public Library build() {
            return new Library(urls, repositories, groupId, artifactId, version, classifier, checksum, relocations, isolatedLoad, loaderId, resolveTransitiveDependencies, excludedTransitiveDependencies);
        }
    }
}
