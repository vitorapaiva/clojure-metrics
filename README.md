# 📊 Clojure Metrics

[![Build Status](https://img.shields.io/badge/tests-passing-brightgreen)](#) [![License](https://img.shields.io/badge/license-EPL--2.0-blue.svg)](#license) [![Clojure](https://img.shields.io/badge/clojure-1.11.1-blue.svg)](https://clojure.org/)

A comprehensive static analysis tool for Clojure code, inspired by PHPMetrics. Calculates important code quality metrics to help with development and maintenance of Clojure projects.

## 🚀 Features

- **Complete static analysis** based on clj-kondo
- **Multiple quality metrics** including cyclomatic complexity, Halstead metrics, and maintainability index
- **Structured JSON output** for easy integration with other tools
- **Simple command-line interface**
- **Recursive directory analysis**
- **Detailed per-file reports and system summary**

## 📈 Calculated Metrics

### 1. Halstead Complexity Measures
- **n1**: Number of unique operators
- **n2**: Number of unique operands  
- **N1**: Total number of operators
- **N2**: Total number of operands
- **Vocabulary (n)**: n1 + n2
- **Length (N)**: N1 + N2
- **Volume**: N * log₂(n)
- **Difficulty**: (n1/2) * (N2/n2)
- **Effort**: Difficulty * Volume

### 2. Cyclomatic Complexity
Measures the number of linearly independent paths through the code. Counts decision points such as:
- Conditionals: `if`, `when`, `cond`, `case`
- Loops: `loop`, `while`, `doseq`, `for`
- Exception handling: `try/catch`
- Logical operators: `and`, `or`

### 3. Maintainability Index
Composite metric that indicates how easy it is to maintain the code:
- **MI = 171 - 5.2 * ln(Halstead Volume) - 0.23 * (Cyclomatic Complexity) - 16.2 * ln(Lines of Code)**
- Includes bonus based on comment density
- Scale from 0-100 (higher is better)

### 4. Lines of Code (LoC)
- **Total lines**: All lines in the file
- **Physical lines**: Non-empty lines
- **Logical lines**: Lines with actual code (excluding comments)
- **Comment lines**: Lines containing only comments
- **Comment density**: Percentage of lines with comments

## 🛠️ Installation

### Prerequisites
- [Leiningen](https://leiningen.org/) 2.0 or higher
- Java 8 or higher

### Clone the repository
```bash
git clone https://github.com/your-username/clojure-metrics.git
cd clojure-metrics
```

### Install dependencies
```bash
lein deps
```

## 📖 Usage

### Analyze a directory
```bash
lein run -- -p src/
```

### Analyze a specific file
```bash
lein run -- -p src/core.clj
```

### Show help
```bash
lein run -- -h
```

### Generate standalone executable
```bash
lein uberjar
java -jar target/uberjar/clojure-metrics-*-standalone.jar -p src/
```

## 📊 JSON Output Format

The tool produces a comprehensive JSON structure with system summary and individual file metrics:

```json
{
  "system-summary": {
    "total-files": 5,
    "length": {
      "cloc": 250,
      "loc": 200,
      "lloc": 180,
      "comment-density": 20.0
    },
    "halstead": {
      "n1": 25,
      "n2": 45,
      "volume": 1250.5,
      "difficulty": 12.5,
      "effort": 15631.25
    },
    "cyclomatic-complexity": 45,
    "average-cyclomatic-complexity": 9.0,
    "maintainability": {
      "index": 75.2,
      "classification": "good",
      "recommendations": ["Consider refactoring functions with high complexity"]
    }
  },
  "files": [...]
}
```

## 📏 Metrics Interpretation

### Cyclomatic Complexity
- **1-10**: Simple, low risk
- **11-20**: Moderate, medium risk
- **21-50**: Complex, high risk
- **>50**: Very complex, very high risk

### Maintainability Index
- **85-100**: Very high maintainability (excellent)
- **70-85**: High maintainability (good)
- **50-70**: Moderate maintainability
- **25-50**: Low maintainability (poor)
- **0-25**: Very low maintainability (critical)

## 🔧 Development

### Run tests
```bash
lein test
```

### Run specific tests
```bash
lein test clojure-metrics.unit.logic.halstead-test
```

### Check code formatting
```bash
lein cljfmt check
```

### Project structure
```
src/
├── clojure_metrics/
│   ├── core.clj              # Main entry point
│   ├── controller/
│   │   └── file_analyzer.clj  # Analysis controller
│   ├── logic/                 # Metrics calculation logic
│   │   ├── cyclomatic.clj
│   │   ├── halstead.clj
│   │   ├── loc.clj
│   │   └── maintainability.clj
│   └── components/
│       └── konjo_analysis.clj # clj-kondo integration

test/
├── clojure_metrics/
│   ├── unit/                  # Unit tests
│   ├── integration/           # Integration tests
│   └── resources/             # Test fixture files
```

## 🤝 Contributing

Contributions are welcome! To contribute:

1. **Fork** the project
2. **Create** a feature branch (`git checkout -b feature/new-feature`)
3. **Commit** your changes (`git commit -am 'Add new feature'`)
4. **Push** to the branch (`git push origin feature/new-feature`)
5. **Open** a Pull Request

### Contribution guidelines
- Keep code well documented
- Add tests for new features
- Follow Clojure code conventions
- Update documentation when necessary

## 🐛 Reporting Issues

Found a bug? Please open an [issue](https://github.com/your-username/clojure-metrics/issues) with:

- Detailed problem description
- Steps to reproduce
- Clojure and Java version
- Sample code (if applicable)

## 📋 Roadmap

- [ ] Support for incremental analysis
- [ ] HTML reports
- [ ] CI/CD integration
- [ ] Additional metrics (coupling, cohesion)
- [ ] Interactive web dashboard
- [ ] Editor plugins (VS Code, IntelliJ)

## 📚 References

- [Cyclomatic Complexity](https://en.wikipedia.org/wiki/Cyclomatic_complexity)
- [Halstead Complexity Measures](https://en.wikipedia.org/wiki/Halstead_complexity_measures)
- [Maintainability Index](https://docs.microsoft.com/en-us/visualstudio/code-quality/code-metrics-values)
- [clj-kondo](https://github.com/clj-kondo/clj-kondo) - Static analysis tool for Clojure

## 📄 License

This project is licensed under the Eclipse Public License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [clj-kondo](https://github.com/clj-kondo/clj-kondo) for excellent syntactic analysis
- [PHPMetrics](https://github.com/phpmetrics/PhpMetrics) for inspiration
- Clojure community for feedback and support

---

**Built with ❤️ for the Clojure community**