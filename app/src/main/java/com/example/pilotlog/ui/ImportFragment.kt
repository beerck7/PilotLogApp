package com.example.pilotlog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pilotlog.databinding.FragmentImportBinding

class ImportFragment : Fragment() {

    private var _binding: FragmentImportBinding? = null
    private val binding get() = _binding!!
    private val flightViewModel: FlightViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView()

        binding.fabImport.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.webview.evaluateJavascript(
                "(function() { return document.documentElement.outerHTML; })();"
            ) { html ->
                val content = unescapeJavaString(html)
                importData(content)
            }
        }
    }

    private fun setupWebView() {
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true
        binding.webview.webViewClient = WebViewClient()
        
        binding.webview.loadUrl("https://ab-pilot.echronometraz.pl/index.php?action=personel&start=loty")
    }

    private fun importData(html: String) {
        flightViewModel.importFromHtml(html, false) { success, count, error ->
            requireActivity().runOnUiThread {
                binding.progressBar.visibility = View.GONE
                if (success) {
                    Toast.makeText(context, "Imported $count flights!", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Import failed: ${error ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun unescapeJavaString(st: String): String {
        var str = st
        if (str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length - 1)
        }
        return str.replace("\\u003C", "<")
            .replace("\\u003E", ">")
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\t", "\t")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
