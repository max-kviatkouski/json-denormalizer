Pseudocode:
toTable(rootJsonNode) = toRow(rootJsonNode) * toTable(allNonPrimitiveChildren)

Algorithm:
1. Get a list of maps representing json
2. Convert to a unified probably sparse table
3. Sort column names by alphabet
4. Stable sort by each column