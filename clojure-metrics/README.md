# Clojure Metrics

Static analysis tool for Clojure code based on clj-kondo, similar to PHPMetrics. Calculates important code quality metrics to help with development and maintenance of Clojure projects.

## Calculated Metrics

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

## Installation

```bash
git clone <repository>
cd clojure-metrics
```

## Usage

### Analyze a directory:
```bash
lein run -- -p src/
```

### Show help:
```bash
lein run -- -h
```

The tool analyzes all `.clj` files in the specified directory recursively and outputs metrics in JSON format to stdout.

## JSON Output Format

The tool outputs a comprehensive JSON structure with both system-wide summary and individual file metrics:

### Output Structure

```json
{
  "system-summary": {
    "total-files": "number - Total number of Clojure files analyzed",
    "length": {
      "cloc": "number - Total lines count (including comments and empty lines)",
      "loc": "number - Lines of code without comments", 
      "lloc": "number - Logical lines of code (without empty lines)",
      "comment-density": "number - Percentage of comment lines (0-100)"
    },
    "halstead": {
      "n1": "number - Number of unique operators",
      "n2": "number - Number of unique operands",
      "N1": "number - Total number of operators", 
      "N2": "number - Total number of operands",
      "vocabulary": "number - n1 + n2",
      "length": "number - N1 + N2",
      "volume": "number - Length * log2(vocabulary)",
      "difficulty": "number - (n1/2) * (N2/n2)",
      "effort": "number - Difficulty * Volume"
    },
    "cyclomatic-complexity": "number - Total cyclomatic complexity across all functions",
    "average-cyclomatic-complexity": "number - Average complexity per function",
    "maintainability": {
      "index": "number - Maintainability Index (0-100, higher is better)",
      "raw-index": "number - Base index before comment bonus",
      "comment-bonus": "number - Bonus points for comment density", 
      "classification": "keyword - excellent, good, moderate, poor, or critical",
      "impact-factors": {
        "volume-impact": "number - Negative impact from Halstead volume",
        "complexity-impact": "number - Negative impact from cyclomatic complexity",
        "loc-impact": "number - Negative impact from lines of code",
        "total-negative-impact": "number - Sum of all negative impacts"
      },
      "recommendations": ["array of strings - Specific recommendations for improvement"]
    },
    "average-maintainability": "number - Average maintainability across all files",
    "structure": {
      "functions": "number - Total number of functions",
      "public-functions": "number - Number of public functions", 
      "private-functions": "number - Number of private functions",
      "macros": "number - Number of macros",
      "dependencies": "number - Number of namespace dependencies",
      "keywords": "number - Number of keywords used",
      "locals": "number - Number of local variables"
    }
  },
  "files": [
    {
      "file": "string - Path to the analyzed file",
      "length": "object - Same structure as system-summary.length",
      "halstead": "object - Same structure as system-summary.halstead plus unique-operators and unique-operands arrays",
      "cyclomatic-complexity": "number - Complexity for this specific file",
      "maintainability": "object - Same structure as system-summary.maintainability", 
      "functions": "number - Number of functions in this file",
      "public-functions": "number - Public functions in this file",
      "private-functions": "number - Private functions in this file", 
      "macros": "number - Macros in this file",
      "dependencies": "number - Dependencies in this file",
      "keywords": "number - Keywords in this file",
      "locals": "number - Local variables in this file"
    }
  ]
}
```

## Sample Output

Here's an example analyzing a single Clojure file:

```json
[
  {
    "file": "test/clojure_metrics/resources/example.clj",
    "length": {
      "cloc": 49,
      "loc": 46,
      "lloc": 42
    },
    "halstead": {
      "n1": 15,
      "n2": 20,
      "N1": 45,
      "N2": 38,
      "vocabulary": 35,
      "length": 83,
      "volume": 425.2,
      "difficulty": 14.25,
      "effort": 6059.1
    },
    "cyclomatic-complexity": 8,
    "maintainability": {
      "index": 73.5,
      "classification": "high",
      "recommendations": ["Well-maintained code", "Keep current good practices"]
    }
  }
]
```

## Metrics Interpretation

### Cyclomatic Complexity
- **1-10**: Simple, low risk
- **11-20**: Moderate, medium risk
- **21-50**: Complex, high risk
- **>50**: Very complex, very high risk

### Maintainability Index
- **85-100**: Very high maintainability
- **70-85**: High maintainability
- **50-70**: Moderate maintainability
- **25-50**: Low maintainability
- **0-25**: Very low maintainability

## Development

### Run tests:
```bash
lein test
```

### Generate executable:
```bash
lein uberjar
java -jar target/uberjar/clojure-metrics-0.1.0-SNAPSHOT-standalone.jar -p src/
```

## Dependencies

- Clojure 1.11.1
- clj-kondo (for syntactic analysis)
- tools.cli (for command line interface)
- data.json (for JSON output)

## Contributing

1. Fork the project
2. Create a branch for your feature
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License

[Specify license]
