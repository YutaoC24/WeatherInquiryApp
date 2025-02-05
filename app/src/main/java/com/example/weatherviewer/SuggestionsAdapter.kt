package com.example.weatherviewer
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class SuggestionsAdapter(context: Context, resource: Int, private val items: List<String>) :
    ArrayAdapter<String>(context, resource, items) {

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val suggestions = mutableListOf<String>()

                if (constraint != null) {
                    for (item in items) {
                        if (item.lowercase().contains(constraint.toString().lowercase())) {
                            suggestions.add(item)
                        }
                    }
                }

                results.values = suggestions
                results.count = suggestions.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                if (results != null && results.count > 0) {
                    addAll(results.values as List<String>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}