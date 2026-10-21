//
// Created by Nitish Yadav on 30/03/23.
//

import Foundation

class testWait {
    let semaphore = DispatchSemaphore(value: 0)
    init() {
        wait()
        DispatchQueue.global(qos: .background).async {
            let thread = waitThread();
            thread.start()
            thread.add();
            self.semaphore.wait()
            thread.sub()
            thread.add()
            self.wait()
            self.semaphore.wait();
            thread.sub()
        }
    }

    func wait(){
        DispatchQueue.global(qos: .background).async {
            Thread.sleep(forTimeInterval: 10)
            self.semaphore.signal()
        }
    }

    private class waitThread: Thread {
//        var semaphore: DispatchSemaphore?
        weak var testWait: testWait?
        var waitTime: TimeInterval = 0
        override func main() {
            print("inside main of waitThread")
            testWait?.semaphore.signal()
        }
        func add(){
            print("inside add");
        }
        func sub(){
            print("inside sub");
        }
    }
}
