//
//  StartScreenViewController.swift
//  ios
//
//  Created by Kevin Galligan on 4/19/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit

class StartScreenViewController: UIViewController{
    override func viewDidAppear(animated: Bool)
    {
        let appScreen = DCPAppManager.findStartScreenWithNSString(nil)
        let segueName = appScreen.name()
        performSegueWithIdentifier(segueName, sender: self)
    }
}