//
//  WelcomeViewController.swift
//  ios
//
//  Created by Kevin Galligan on 4/19/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit

class WelcomeViewController: UIViewController
{
    @IBOutlet var doneButton: UIButton!
    
    @IBAction func doneClicked(sender: AnyObject) {
        DCPAppManager.getAppPrefs().setHasSeenWelcome()
        performSegueWithIdentifier("Login", sender: self)
        
    }
}
