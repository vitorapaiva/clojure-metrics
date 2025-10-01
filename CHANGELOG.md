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

## [0.0.2] - 2025-09-30

### Added
- Release version 0.0.2
- [Add your changes here]

- Support for incremental analysis
- HTML report generation
- CI/CD integration
- Additional metrics (coupling, cohesion)
- Interactive web dashboard
- Editor plugins (VS Code, IntelliJ)

## [0.0.1] - 2025-09-21

### Added
- Release version 0.0.1
- [Add your changes here]

- Support for incremental analysis
- HTML report generation
- CI/CD integration
- Additional metrics (coupling, cohesion)
- Interactive web dashboard
- Editor plugins (VS Code, IntelliJ)

## [0.0.1] - 2025-09-22

### Added
- **Core Analysis Engine**: Complete static analysis based on clj-kondo
- **Halstead Complexity Measures**: Comprehensive calculation of all Halstead metrics
  - Unique and total operators/operands count
  - Vocabulary, length, volume, difficulty, and effort calculations
- **Cyclomatic Complexity**: Analysis of control flow complexity
  - Support for conditionals, loops, exception handling, and logical operators
- **Maintainability Index**: Composite metric for code maintainability
  - Considers Halstead volume, cyclomatic complexity, and lines of code
  - Includes comment density bonus
  - Classification system (excellent, good, moderate, poor, critical)
- **Lines of Code Analysis**: Multiple LoC measurements
  - Total lines, physical lines, logical lines, comment lines
  - Comment density calculation
- **Structured Analysis Output**: Comprehensive JSON format
  - System-wide summary with aggregated metrics
  - Individual file analysis results
  - Detailed recommendations for improvement
- **Command Line Interface**: Simple and intuitive CLI
  - Directory and single file analysis
  - Configurable output options
- **Comprehensive Test Suite**: 
  - 41 unit and integration tests
  - 322 test assertions
  - Complete coverage of all metrics calculations
- **Project Structure**: Well-organized codebase
  - Modular architecture with separated concerns
  - Clean separation between logic, controllers, and components

### Technical Details
- Built with Clojure 1.11.1
- Uses clj-kondo for syntactic analysis
- JSON output for easy integration
- Recursive directory processing
- Error handling and validation

[Unreleased]: https://github.com/your-username/clojure-metrics/compare/v0.0.2...HEAD
[0.0.2]: https://github.com/your-username/clojure-metrics/releases/tag/v0.0.2[0.0.1]: https://github.com/your-username/clojure-metrics/releases/tag/v0.0.1[0.0.1]: https://github.com/your-username/clojure-metrics/releases/tag/v0.0.1