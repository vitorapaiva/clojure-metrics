# Release Process

This document describes the release process for Clojure Metrics.

## Quick Release

For a quick release using the automated script:

```bash
# Make sure you're on the main branch and have no uncommitted changes
git checkout main
git pull origin main

# Run the release script
./scripts/release.sh 0.0.2

# Push changes and tags
git push origin main --tags

# Create GitHub release (manual step)
```

## Manual Release Process

If you prefer to do releases manually or need more control:

### 1. Prepare the Release

```bash
# Ensure you're on main branch
git checkout main
git pull origin main

# Run tests to ensure everything works
lein test

# Build to verify compilation
lein uberjar
```

### 2. Update Version

Edit `project.clj`:
```clojure
(defproject clojure-metrics "0.0.2"  ; Update version here
  :description "Comprehensive static analysis tool for Clojure code with quality metrics"
  ; ... rest of configuration
```

### 3. Update Changelog

Edit `CHANGELOG.md` to add the new version:

```markdown
## [0.0.2] - 2025-09-22

### Added
- New feature X
- Improvement Y

### Fixed
- Bug fix Z

### Changed
- Updated dependency versions
```

### 4. Commit and Tag

```bash
# Commit version changes
git add project.clj CHANGELOG.md
git commit -m "Release v0.0.2"

# Create annotated tag
git tag -a v0.0.2 -m "Release version 0.0.2"

# Push changes and tags
git push origin main --tags
```

### 5. Create GitHub Release

1. Go to the [GitHub Releases page](https://github.com/your-username/clojure-metrics/releases)
2. Click "Create a new release"
3. Choose the tag you just created (v0.0.2)
4. Set release title: "v0.0.2"
5. Copy the changelog section for this version into the description
6. Attach the built JAR file from `target/uberjar/`
7. Click "Publish release"

## Release Types

### Patch Release (0.0.x)

For bug fixes and small improvements:

```bash
./scripts/release.sh 0.0.2
```

What goes in a patch release:
- Bug fixes
- Documentation updates
- Performance improvements
- Security fixes

### Minor Release (0.y.0)

For new features that are backwards compatible:

```bash
./scripts/release.sh 0.1.0
```

What goes in a minor release:
- New metrics
- New output formats
- New CLI options (backwards compatible)
- Dependencies updates
- New features

### Major Release (x.0.0)

For breaking changes (when we reach 1.0.0):

```bash
./scripts/release.sh 1.0.0
```

What goes in a major release:
- Breaking API changes
- Removal of deprecated features
- Major architecture changes
- CLI interface changes

## Pre-release Checklist

- [ ] All tests pass (`lein test`)
- [ ] Project builds successfully (`lein uberjar`)
- [ ] CLI works correctly (`java -jar target/uberjar/clojure-metrics-*-standalone.jar -h`)
- [ ] Documentation is up to date
- [ ] CHANGELOG.md includes all changes
- [ ] Version number follows semantic versioning
- [ ] No uncommitted changes

## Post-release Checklist

- [ ] GitHub release created with release notes
- [ ] JAR artifact attached to GitHub release
- [ ] Social media announcement (if applicable)
- [ ] Update any dependent projects
- [ ] Close related issues and pull requests

## Distribution

The primary distribution method is through GitHub releases:

1. **Build artifacts** are attached to GitHub releases
2. **Users can download** the standalone JAR from releases page
3. **Source code** is available for building from source

## Version Strategy

### Development Phase (0.y.z)
- We're currently in this phase
- Breaking changes are allowed between minor versions
- API is not stable yet

### Stable Phase (1.y.z)
- Will start when API is finalized
- Semantic versioning strictly followed
- Breaking changes only in major versions

## Rollback Process

If a release has issues:

### Quick Rollback
```bash
# Revert the release commit
git revert HEAD

# Remove the problematic tag
git tag -d v0.0.2
git push origin :refs/tags/v0.0.2

# Push the revert
git push origin main
```

### GitHub Release
1. Mark the GitHub release as "Pre-release" 
2. Create a new discussion explaining the issue
3. Plan and execute a patch release

## Automation Roadmap

Future improvements to the release process:

- [ ] Automatic changelog generation from commit messages
- [ ] Automatic version bumping based on conventional commits
- [ ] GitHub Actions for automated releases
- [ ] Automatic artifact publishing on tag creation
- [ ] Release candidate (RC) versions for testing
- [ ] Automatic dependency vulnerability scanning

## Release Calendar

We aim to follow this release schedule:

- **Patch releases**: As needed for critical bugs
- **Minor releases**: Monthly (when new features are ready)
- **Major releases**: When breaking changes are necessary

## Communication

### Release Announcements
- GitHub Releases (primary)
- Project README.md update
- Community channels (if applicable)

### Breaking Changes
- Advance notice in previous release notes
- Migration guide in documentation
- Clear communication in CHANGELOG.md
