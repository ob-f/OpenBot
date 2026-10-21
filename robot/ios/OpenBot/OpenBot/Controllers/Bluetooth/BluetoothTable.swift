//
// Created by Nitish Yadav on 18/10/22.
//

import UIKit
import CoreBluetooth

class BluetoothTable: UITableViewController {
    var bluetooth = bluetoothDataController.shared
    var centralManager: CBCentralManager?
    var isconnected: Bool = false

    /// Called after the view controller has loaded.
    override func viewDidLoad() {
        super.viewDidLoad()
        clearsSelectionOnViewWillAppear = false
        setupNavigationBarItem();
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothConnected, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateConnect), name: .bluetoothDisconnected, object: nil)
    }

    /// Notifies the view controller that its view is about to be added to a view hierarchy.
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        bluetooth.startScan()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        bluetooth.sendData(payload: "f\n");
    }

    /**
     Function to connect with selected bluetooth
     - Parameters:
       - Btn:
       - index:
     */
    func connectToBle(Btn: UIButton, index: Int) {
        bluetooth.peri = bluetooth.peripherals[index]
        bluetooth.connect()
        bluetooth.centralManager?.stopScan()
        isBluetoothConnected = true
    }

    /**
     Function to disconnect from bluetooth
     */
    func disconnectToBle() {
        bluetooth.disconnect()
        tableView.reloadData()
        isBluetoothConnected = false
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        bluetooth.peripherals.count
    }

    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        super.tableView(tableView, heightForRowAt: indexPath)
        return 44
    }

    /**
     Delegate to setup each cell of table
     - Parameters:
       - tableView:
       - indexPath:
     - Returns:
     */
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "myCell", for: indexPath) as! BluetoothTableViewCell
        let peripheral = bluetooth.peripherals[indexPath.row]
        cell.textLabel?.text = peripheral.name
        cell.connectButton.addTarget(self, action: #selector(connect(sender:)), for: .touchUpInside)
        cell.connectButton.tag = indexPath.row
        switch peripheral.state {
        case .disconnected:
            cell.connectButton.setTitle(Strings.connect, for: .normal)
            cell.textLabel?.textColor = Colors.border
            cell.connectButton.setTitleColor(Colors.border, for: .normal)
        case .connected:
            cell.connectButton.setTitle(Strings.disconnect, for: .normal)
            cell.textLabel?.textColor = Colors.title
            cell.connectButton.setTitleColor(UIColor.red, for: .normal)
        case .connecting:
            cell.connectButton.setTitle(Strings.connecting, for: .normal)
            cell.connectButton.setTitleColor(UIColor.green, for: .normal)
        case .disconnecting:
            cell.connectButton.setTitle(Strings.disconnecting, for: .normal)
        @unknown default:
            cell.connectButton.setTitle(Strings.connect, for: .normal)
        }
        return cell
    }

    @objc func updateConnect(_ notification: Notification) {
        tableView.reloadData()
    }

    /**
     Handler function of connect button
     - Parameter sender:
     */
    @objc func connect(sender: UIButton) {

        if isBluetoothConnected {
            disconnectToBle();
            if sender.title(for: .normal) == Strings.connect {
                connectToBle(Btn: sender, index: sender.tag)
            }
        } else {
            connectToBle(Btn: sender, index: sender.tag)
        }
        tableView.reloadData()
    }

    /**
     Function to customise the navigation bar
     */
    func setupNavigationBarItem() {
        if UIImage(named: "back") != nil {
            let backNavigationIcon = (UIImage(named: "back")?.withRenderingMode(.alwaysOriginal))!
            let newBackButton = UIBarButtonItem(image: backNavigationIcon, title: Strings.bluetooth, target: self, action: #selector(back(sender:)), titleColor: Colors.navigationColor ?? .white)
            navigationItem.leftBarButtonItem = newBackButton
        }
    }

    /**
        function remove current view controller from the navigation controller
     - Parameter sender:
     */
    @objc func back(sender: UIBarButtonItem) {
        _ = navigationController?.popViewController(animated: true)
    }
}

class BluetoothTableViewCell: UITableViewCell {
    @IBOutlet weak var connectButton: UIButton!
}
