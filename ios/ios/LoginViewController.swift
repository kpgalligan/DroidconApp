//
//  LoginViewController.swift
//  ios
//
//  Created by Kevin Galligan on 4/19/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit
import Crashlytics


class LoginViewController: UIViewController, DCPLoginScreenPresenter_Host, GIDSignInUIDelegate
{
    var presenter: DCPLoginScreenPresenter?
    
    override func viewDidLoad() {
        presenter = DCPLoginScreenPresenter(androidContentContext: DCPAppManager.getContext(), withDCPLoginScreenPresenter_Host: self)
        GIDSignIn.sharedInstance().uiDelegate = self
        let viewController = UIApplication.sharedApplication().keyWindow?.rootViewController;
        NSLog("heyo")
        NSLog((viewController?.title)!)
        
        let button = UIButton(type: UIButtonType.RoundedRect)
        button.frame = CGRectMake(20, 50, 100, 30)
        button.setTitle("Crash", forState: UIControlState.Normal)
        button.addTarget(self, action: "crashButtonTapped:", forControlEvents: UIControlEvents.TouchUpInside)
        view.addSubview(button)

    }
    
    @IBAction func crashButtonTapped(sender: AnyObject) {
        Crashlytics.sharedInstance().crash()
    }

    
    func loggedIn(user: GIDGoogleUser)
    {
        presenter?.runGoogleLoginWithNSString(user.authentication.idToken, withNSString: user.profile.name, withNSString: nil, withNSString: nil)
    }
    
    @objc func onLoginReturnedWithBoolean(failed: jboolean, withBoolean firstLogin: jboolean)
    {
        if(!failed)
        {
            performSegueWithIdentifier("Schedule", sender: self)
        }
    }
    
//    - (void)loggedIn:(NSString *)token
//    withName:(NSString *)name
//    {
//    [self.dataPresenter loginUserWithNSString:token withNSString:name];
//    }
}
