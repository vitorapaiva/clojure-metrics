#!/bin/bash

# Release script for clojure-metrics
# Usage: ./scripts/release.sh <version>
# Example: ./scripts/release.sh 0.0.2

set -e

if [ $# -eq 0 ]; then
    echo "Error: Version number required"
    echo "Usage: $0 <version>"
    echo "Example: $0 0.0.2"
    exit 1
fi

VERSION=$1
CURRENT_BRANCH=$(git branch --show-current)

# Validate version format (basic semver check)
if ! [[ $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: Invalid version format. Use semantic versioning (e.g., 0.0.2)"
    exit 1
fi

echo "🚀 Starting release process for version $VERSION"

# Check if we're on main branch
if [ "$CURRENT_BRANCH" != "main" ] && [ "$CURRENT_BRANCH" != "master" ]; then
    echo "⚠️  Warning: You're not on the main branch (current: $CURRENT_BRANCH)"
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Release cancelled"
        exit 1
    fi
fi

# Check for uncommitted changes
if ! git diff-index --quiet HEAD --; then
    echo "❌ Error: You have uncommitted changes. Please commit or stash them first."
    exit 1
fi

# Check if tag already exists
if git tag -l | grep -q "^v$VERSION$"; then
    echo "❌ Error: Tag v$VERSION already exists"
    exit 1
fi

echo "📋 Pre-release checks..."

# Run tests
echo "🧪 Running tests..."
if ! lein test; then
    echo "❌ Error: Tests failed"
    exit 1
fi
echo "✅ Tests passed"

# Check if project compiles
echo "🔨 Checking compilation..."
if ! lein compile; then
    echo "❌ Error: Compilation failed"
    exit 1
fi
echo "✅ Compilation successful"

# Update version in project.clj
echo "📝 Updating version in project.clj..."
sed -i.bak "s/(defproject clojure-metrics \"[^\"]*\"/(defproject clojure-metrics \"$VERSION\"/" project.clj
rm project.clj.bak

# Update CHANGELOG.md
echo "📝 Updating CHANGELOG.md..."
TODAY=$(date +%Y-%m-%d)

# Create a temporary file with the new changelog entry
cat > /tmp/new_changelog << EOF
# Changelog

All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

### Planned
- Support for incremental analysis
- HTML report generation
- CI/CD integration
- Additional metrics (coupling, cohesion)
- Interactive web dashboard
- Editor plugins (VS Code, IntelliJ)

## [$VERSION] - $TODAY

### Added
- Release version $VERSION
- [Add your changes here]

EOF

# Append the rest of the changelog (excluding the header)
tail -n +8 CHANGELOG.md >> /tmp/new_changelog
mv /tmp/new_changelog CHANGELOG.md

# Update the links at the bottom
sed -i.bak "s|\[Unreleased\]:.*|[Unreleased]: https://github.com/your-username/clojure-metrics/compare/v$VERSION...HEAD|" CHANGELOG.md
sed -i.bak "/\[Unreleased\]:/a\\
[$VERSION]: https://github.com/your-username/clojure-metrics/releases/tag/v$VERSION" CHANGELOG.md
rm CHANGELOG.md.bak

echo "✅ Version updated to $VERSION"

# Build the project
echo "🔨 Building uberjar..."
if ! lein uberjar; then
    echo "❌ Error: Build failed"
    exit 1
fi
echo "✅ Build successful"

# Commit changes
echo "📝 Committing changes..."
git add project.clj CHANGELOG.md
git commit -m "Release v$VERSION

- Update version to $VERSION
- Update CHANGELOG.md with release notes"

# Create tag
echo "🏷️  Creating tag v$VERSION..."
git tag -a "v$VERSION" -m "Release version $VERSION

See CHANGELOG.md for details."

echo "✅ Release v$VERSION prepared successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Review the changes: git show"
echo "2. Push to remote: git push origin $CURRENT_BRANCH --tags"
echo "3. Create GitHub release from tag v$VERSION"
echo "4. Update project documentation if needed"
echo ""
echo "🎉 Release process completed!"
