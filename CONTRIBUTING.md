# Contributing to Clojure Metrics

Thank you for your interest in contributing to Clojure Metrics! This document provides guidelines and information for contributors.

## 🚀 Getting Started

### Prerequisites
- [Leiningen](https://leiningen.org/) 2.0 or higher
- Java 8 or higher
- Git

### Setting up the development environment

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/your-username/clojure-metrics.git
   cd clojure-metrics
   ```
3. **Install dependencies**:
   ```bash
   lein deps
   ```
4. **Run tests** to ensure everything works:
   ```bash
   lein test
   ```

## 🔄 Development Workflow

### Making Changes

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following our coding standards

3. **Add tests** for new functionality

4. **Run the test suite**:
   ```bash
   lein test
   ```

5. **Check code formatting** (if cljfmt is configured):
   ```bash
   lein cljfmt check
   ```

6. **Commit your changes**:
   ```bash
   git commit -am "Add descriptive commit message"
   ```

7. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

8. **Open a Pull Request** on GitHub

### Branch Naming
- `feature/description` - for new features
- `fix/description` - for bug fixes
- `docs/description` - for documentation updates
- `refactor/description` - for code refactoring

## 📝 Coding Standards

### Clojure Style Guidelines
- Follow [Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide)
- Use meaningful function and variable names
- Write docstrings for public functions
- Keep functions small and focused
- Prefer pure functions when possible

### Code Organization
- Place new metrics in the appropriate `logic/` namespace
- Add controller logic to `controller/` namespace
- Keep tests organized in parallel structure under `test/`

### Documentation
- Add docstrings to all public functions
- Include usage examples in docstrings
- Update README.md for new features
- Add entries to CHANGELOG.md

## 🧪 Testing

### Running Tests
```bash
# Run all tests
lein test

# Run specific test namespace
lein test clojure-metrics.unit.logic.halstead-test

# Run specific test
lein test :only clojure-metrics.unit.logic.halstead-test/test-count-halstead
```

### Writing Tests
- Write tests for all new functionality
- Include both positive and negative test cases
- Use descriptive test names
- Add integration tests for end-to-end functionality
- Aim for high test coverage

### Test Structure
```clojure
(deftest test-descriptive-name
  (testing "What this test validates"
    (is (= expected-result (function-under-test input))
        "Descriptive error message")))
```

## 📋 Types of Contributions

### 🐛 Bug Fixes
- Check existing issues before creating new ones
- Include reproduction steps
- Add tests that would have caught the bug
- Keep fixes focused and minimal

### ✨ New Features

#### Adding New Metrics
1. Create the metric calculation logic in `src/clojure_metrics/logic/`
2. Add integration in the file analyzer
3. Update JSON output schema
4. Add comprehensive tests
5. Update documentation

#### Example: Adding a new metric
```clojure
(ns clojure-metrics.logic.new-metric
  "Description of the new metric and its purpose.")

(defn calculate-new-metric
  "Calculate the new metric for the given AST data.
   
   Parameters:
   - ast-data: clj-kondo analysis output
   
   Returns:
   - Map with metric results"
  [ast-data]
  ;; Implementation here
  )
```

### 📚 Documentation
- Fix typos and grammatical errors
- Improve clarity of existing documentation
- Add examples and use cases
- Translate documentation (if applicable)

### 🔧 Infrastructure
- CI/CD improvements
- Build process enhancements
- Development tooling
- Performance optimizations

## 🚦 Pull Request Process

### Before Submitting
- [ ] Tests pass locally
- [ ] Code follows style guidelines
- [ ] Documentation is updated
- [ ] CHANGELOG.md is updated (for features)
- [ ] Commit messages are clear and descriptive

### Pull Request Template
When opening a PR, please include:

1. **Description**: What does this PR do?
2. **Motivation**: Why is this change needed?
3. **Type of Change**: Bug fix, feature, documentation, etc.
4. **Testing**: How was this tested?
5. **Breaking Changes**: Any backward compatibility issues?

### Review Process
1. Automated tests must pass
2. At least one maintainer review required
3. Address any feedback or requested changes
4. Maintainer will merge after approval

## 🐛 Reporting Issues

### Bug Reports
Include:
- **Environment**: OS, Java version, Leiningen version
- **Steps to reproduce**: Minimal example
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Error messages**: Full stack traces if applicable

### Feature Requests
Include:
- **Use case**: Why is this feature needed?
- **Proposed solution**: How should it work?
- **Alternatives**: Other ways to solve the problem
- **Additional context**: Related issues or background

## 🏷️ Issue Labels

- `bug` - Something isn't working
- `enhancement` - New feature request
- `documentation` - Improvements to docs
- `good first issue` - Good for newcomers
- `help wanted` - Community help requested
- `question` - Further information requested

## 💬 Communication

### Getting Help
- **GitHub Issues**: For bugs and feature requests
- **GitHub Discussions**: For questions and general discussion
- **Email**: For private inquiries (see README)

### Code of Conduct
This project follows the [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/). Please be respectful and inclusive in all interactions.

## 🏆 Recognition

Contributors will be recognized in:
- README.md acknowledgments section
- CHANGELOG.md for significant contributions
- GitHub contributors page

## 📚 Resources

### Learning Clojure
- [Clojure Official Documentation](https://clojure.org/)
- [Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide)
- [Clojure Testing](https://clojure.org/guides/deps_and_cli#_running_tests)

### Code Quality Metrics
- [Cyclomatic Complexity](https://en.wikipedia.org/wiki/Cyclomatic_complexity)
- [Halstead Complexity Measures](https://en.wikipedia.org/wiki/Halstead_complexity_measures)
- [Maintainability Index](https://docs.microsoft.com/en-us/visualstudio/code-quality/code-metrics-values)

### Static Analysis
- [clj-kondo Documentation](https://github.com/clj-kondo/clj-kondo)
- [AST Analysis in Clojure](https://clojure.org/reference/compilation)

Thank you for contributing to Clojure Metrics! 🎉
