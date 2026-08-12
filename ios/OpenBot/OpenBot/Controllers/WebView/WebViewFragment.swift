//
// Created by Nitish Yadav on 15/05/23.
//

import Foundation
import UIKit
import WebKit

/**
 class for webview
 */
class openCodeWebView: UIViewController, WKUIDelegate, WKNavigationDelegate {
    var webView: WKWebView!
    var newWebviewPopupWindow: WKWebView?

    /**
     Function to configure webview before its load
     */
    override func loadView() {
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.applicationNameForUserAgent = "Mozilla/6.0 (iPhone; CPU iPhone OS 16_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.3 Mobile/15E148 Safari/604.1"
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.configuration.defaultWebpagePreferences.allowsContentJavaScript = true
        webView.scrollView.panGestureRecognizer.isEnabled = false
        webView.scrollView.pinchGestureRecognizer?.isEnabled = false
        webView.isOpaque = false
        webView.uiDelegate = self
        view = webView
        webView.configuration.userContentController.addUserScript(self.getZoomDisableScript())
        // Add userContentController with WKUserScript to log JavaScript errors
        let userContentController = WKUserContentController()
        let userScriptSource = "window.onerror = function(message, source, lineno, colno, error) { console.log('JavaScript Error: ' + message + ' at ' + source + ':' + lineno); };"
        let userScript = WKUserScript(source: userScriptSource, injectionTime: .atDocumentStart, forMainFrameOnly: true)
        userContentController.addUserScript(userScript)
        webConfiguration.userContentController = userContentController
    }

    /**
     Function called after view loaded and request to load the url
     */
    override func viewDidLoad() {
        super.viewDidLoad()
        webView.navigationDelegate = self
        let myURL = URL(string: "https://www.playground.openbot.org/")
        let myRequest = URLRequest(url: myURL!)
        let scrollView = webView.scrollView
        scrollView.showsVerticalScrollIndicator = false
        scrollView.showsHorizontalScrollIndicator = false
        webView.isUserInteractionEnabled = true
        webView.allowsLinkPreview = false
        webView.configuration.preferences.javaScriptEnabled = true
        webView.load(myRequest)
    }

    /**
     Delegate called after webview loaded
     - Parameters:
       - webView:
       - navigation:
     */
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        print("didFinish")
        // Access cookies
//        webView.configuration.websiteDataStore.httpCookieStore.getAllCookies { cookies in
//            for cookie in cookies {
//                print("Cookie: \(cookie.name) = \(cookie.value)")
//            }
//        }

        webView.evaluateJavaScript("Object.entries(window.localStorage)") { result, error in
            if let error = error {
                print("Error accessing local storage: \(error)")
            } else if let entries = result as? [[String]] {
                for entry in entries {
                    let key = entry[0]
                    let value = entry[1]
                    print("Key: \(key), Value: \(value)")
                }
            }
        }
    }

    /**
     Function to set the UI of popups
     - Parameters:
       - webView:
       - configuration:
       - navigationAction:
       - windowFeatures:
     - Returns:
     */
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        newWebviewPopupWindow = WKWebView(frame: view.bounds, configuration: configuration)
        print("inside newWebviewPopupWindow")
        newWebviewPopupWindow!.backgroundColor = UIColor.clear // Set background color to clear
        newWebviewPopupWindow!.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        newWebviewPopupWindow!.navigationDelegate = self
        newWebviewPopupWindow!.uiDelegate = self
        view.addSubview(newWebviewPopupWindow!)
        return newWebviewPopupWindow!
    }


    func webViewDidClose(_ webView: WKWebView) {
        webView.removeFromSuperview()
        print("hello bc")
        newWebviewPopupWindow = nil
    }

    /**
     function to inject script to prevent zoom of webView
     - Returns:
     */
    private func getZoomDisableScript() -> WKUserScript {
        let source: String = "var meta = document.createElement('meta');" +
                "meta.name = 'viewport';" +
                "meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';" +
                "var head = document.getElementsByTagName('head')[0];" + "head.appendChild(meta);"
        return WKUserScript(source: source, injectionTime: .atDocumentEnd, forMainFrameOnly: true)
    }

    /**
     Fucntion to add alert box if there is any alert in the website.
     - Parameters:
       - webView:
       - message:
       - frame:
       - completionHandler:
     */
    func webView(_ webView: WKWebView, runJavaScriptTextInputPanelWithPrompt prompt: String, defaultText: String?, initiatedByFrame frame: WKFrameInfo,
                 completionHandler: @escaping (String?) -> Void) {
        let alertController = UIAlertController(title: nil, message: prompt, preferredStyle: .alert)

        alertController.addTextField { (textField) in
            textField.text = defaultText
        }

        alertController.addAction(UIAlertAction(title: Strings.okAction, style: .default, handler: { (action) in
            if let text = alertController.textFields?.first?.text {
                completionHandler(text)
            } else {
                completionHandler(defaultText)
            }
        }))

        alertController.addAction(UIAlertAction(title: Strings.cancel, style: .cancel, handler: { (action) in
            completionHandler(nil)
        }))

        present(alertController, animated: true, completion: nil)
    }

}
