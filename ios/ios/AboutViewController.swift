//
//  AboutViewController.swift
//  ios
//
//  Created by Ramona Harrison on 7/26/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit

class AboutViewController: UIViewController {

    @IBOutlet weak var navBar: UINavigationBar!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        navBar.translucent = false
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

}
