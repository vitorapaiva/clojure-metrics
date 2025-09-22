# Versioning Guidelines

Clojure Metrics follows [Semantic Versioning 2.0.0](https://semver.org/).

## Version Format

Given a version number `MAJOR.MINOR.PATCH`, increment the:

1. **MAJOR** version when you make incompatible API changes
2. **MINOR** version when you add functionality in a backwards compatible manner
3. **PATCH** version when you make backwards compatible bug fixes

## Current Version: 0.0.1

This is the initial release version. Since we're still in early development (0.y.z), the API is not stable and may change between versions.

## Version History and Rationale

### 0.0.1 (2025-09-22) - Initial Release
**Why 0.0.1 instead of 1.0.0?**
- This is the first functional release but still in early development
- API may change as we gather user feedback
- Some planned features are not yet implemented
- Following semver guidelines for initial development phase

## Pre-1.0.0 Development Phase

During the 0.y.z phase:
- **0.0.z**: Initial development, basic functionality
- **0.1.z**: First stable API draft, core features complete
- **0.2.z**: Enhanced features, improved stability
- **0.y.0**: Minor version increments for new features
- **0.y.z**: Patch increments for bug fixes

## Release Process

### Creating a New Release

1. **Update version** in `project.clj`
2. **Update CHANGELOG.md** with new version entry
3. **Commit changes**: `git commit -am "Release v0.0.1"`
4. **Create tag**: `git tag -a v0.0.1 -m "Release version 0.0.1"`
5. **Push changes**: `git push origin main --tags`
6. **Create GitHub release** from the tag

### Version Bump Guidelines

#### Patch Version (0.0.x)
- Bug fixes
- Documentation updates
- Performance improvements (no API changes)
- Internal refactoring

#### Minor Version (0.y.0)
- New features
- New metrics implementation
- New output formats
- Backwards compatible API changes

#### Major Version (1.0.0 and beyond)
- Breaking API changes
- Removal of deprecated features
- Significant architecture changes

## API Stability Promise

### Current (0.y.z) - Development Phase
- **CLI Interface**: Relatively stable, minor changes possible
- **JSON Output**: May evolve, new fields may be added
- **Programmatic API**: Not yet public, may change significantly

### Future (1.0.0+) - Stable Phase
- **CLI Interface**: Backwards compatible, deprecated options supported for one major version
- **JSON Output**: Additive changes only, existing fields maintained
- **Programmatic API**: Semantic versioning strictly followed

## Deprecation Policy

### Pre-1.0.0
- Features may be removed without deprecation warnings
- Changes documented in CHANGELOG.md

### Post-1.0.0
- Deprecated features marked in documentation
- Deprecation warnings in code/output
- Deprecated features removed only in major version bumps
- Minimum one minor version before removal

## Examples of Version Changes

### Patch Release (0.0.1 → 0.0.2)
```
- Fix calculation error in cyclomatic complexity
- Improve error messages
- Update documentation
```

### Minor Release (0.0.2 → 0.1.0)
```
- Add HTML output format
- Implement new coupling metric
- Add configuration file support
- Backwards compatible CLI changes
```

### Major Release (0.9.0 → 1.0.0)
```
- API finalization
- Remove deprecated options
- Change default output format
- Stable API guarantee
```

## Branch Strategy

- **main**: Current stable version
- **develop**: Next minor version development
- **feature/\***: Feature development branches
- **hotfix/\***: Urgent fixes for current version

## Release Branches

For major/minor releases:
1. Create release branch: `release/0.1.0`
2. Finalize features and fix bugs
3. Update version and changelog
4. Merge to main and develop
5. Tag release

## Automation

### GitHub Actions
- Automatic testing on all supported Java versions
- Build verification for each commit
- Release artifact generation on tags

### Future Enhancements
- Automatic version bumping
- Changelog generation from commits
- Release notes automation
- GitHub release automation

## Communication

### Release Announcements
- GitHub Releases with detailed changelog
- Update README.md with latest version
- Community announcements (if applicable)

### Breaking Changes
- Clearly documented in CHANGELOG.md
- Migration guides provided
- Advance notice in previous versions (when possible)

## Tools and Scripts

### Version Bump Script (Future)
```bash
./scripts/bump-version.sh patch|minor|major
```

### Release Checklist
- [ ] All tests passing
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Version bumped in project.clj
- [ ] Tag created and pushed
- [ ] GitHub release created
- [ ] GitHub release created with artifacts

## References

- [Semantic Versioning 2.0.0](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)
- [Clojure Library Versioning](https://github.com/clojure/clojure/blob/master/doc/version-history.txt)
