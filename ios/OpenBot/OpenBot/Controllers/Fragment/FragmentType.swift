//
//  FragmentType.swift
//  OpenBot
//
//  Created by Hardik Garg on 22/12/23.
//

import Foundation

class FragmentType {
    
    static let shared : FragmentType = FragmentType();
    
    var currentFragment : String = ""
    
    func closeFragment() -> FragmentTypeEvent {
        return FragmentTypeEvent(status: .init(FRAGMENT_TYPE: "CLOSE"))
    }
}
