// inside mongodb console.
// check the data structure.
var schemaObj= db.Loans.findOne();
// Print mongo schema
function printSchema(obj, indent) {
        // Go over keys in objects
        for (var key in obj) {
            print(indent, key, typeof obj[key]) ;
            // Print object property. Add indentation.
            if (typeof obj[key] == "object") {
                printSchema(obj[key], indent + "\t")
            }
        }
    };
// Call above function.
printSchema(schemaObj,"");