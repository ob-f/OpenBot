//
// Created by Sparsh Jain on 04/10/22.
//

import Foundation

/// The FileDownloader class allows downloading files from a given URL either asynchronously or synchronously.
class FileDownloader {

    /// This method downloads the file synchronously and saves it to the document directory.
    /// If the file already exists, it will return the path of the file. If the file is downloaded and saved successfully, it will return the path of the saved file.
    ///
    /// - Parameters:
    ///     - url: the url of the file to be downloaded
    ///     - fileName: name of the file to be downloaded
    ///     - completion: closure of the asynchronous function
    static func loadFileSync(url: URL, fileName: String, completion: @escaping (String?, Error?) -> Void) {
        let documentsUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let destinationUrl = documentsUrl.appendingPathComponent(fileName)

        if FileManager().fileExists(atPath: destinationUrl.path) {
            print("File already exists [\(destinationUrl.path)]")
            completion(destinationUrl.path, nil)
        } else if let dataFromURL = NSData(contentsOf: url) {
            if dataFromURL.write(to: destinationUrl, atomically: true) {
                print("file saved [\(destinationUrl.path)]")
                completion(destinationUrl.path, nil)
            } else {
                print("error saving file")
                let error = NSError(domain: "Error saving file", code: 1001, userInfo: nil)
                completion(destinationUrl.path, error)
            }
        } else {
            let error = NSError(domain: "Error downloading file", code: 1002, userInfo: nil)
            completion(destinationUrl.path, error)
        }
    }

    /// This method downloads the file asynchronously using URLSession and saves it to the document directory.
    /// If the file already exists, it will return the path of the file. If the file is downloaded and saved successfully, it will return the path of the saved file.
    ///
    /// - Parameters:
    ///     - url: the url of the file to be downloaded
    ///     - fileName: name of the file to be downloaded
    ///     - completion: closure of the asynchronous function
    static func loadFileAsync(url: URL, completion: @escaping (String?, Error?) -> Void, fileName: String?) {
        let documentsUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let destinationUrl = documentsUrl.appendingPathComponent(fileName ?? url.lastPathComponent)

        if FileManager().fileExists(atPath: destinationUrl.path) {
            print("File already exists [\(destinationUrl.path)]")
            completion(destinationUrl.path, nil)
        } else {
            let session = URLSession(configuration: URLSessionConfiguration.default, delegate: nil, delegateQueue: nil)
            var request = URLRequest(url: url)
            request.httpMethod = "GET"
            let task = session.dataTask(with: request, completionHandler:
            {
                data, response, error in
                if error == nil {
                    if let response = response as? HTTPURLResponse {
                        if response.statusCode == 200 {
                            if let data = data {
                                if let _ = try? data.write(to: destinationUrl, options: Data.WritingOptions.atomic) {
                                    completion(destinationUrl.path, error)
                                } else {
                                    completion(destinationUrl.path, error)
                                }
                            } else {
                                completion(destinationUrl.path, error)
                            }
                        }
                    }
                } else {
                    completion(destinationUrl.path, error)
                }
            })
            task.resume()
        }
    }
}
