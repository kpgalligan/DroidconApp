//
//  LoginViewController.swift
//  ios
//
//  Created by Kevin Galligan on 4/19/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit


class LoginViewController: UIViewController, DCPLoginScreenPresenter_Host, GIDSignInUIDelegate
{
    var presenter: DCPLoginScreenPresenter?
    
    override func viewDidLoad() {
        presenter = DCPLoginScreenPresenter(androidContentContext: DCPAppManager.getContext(), withDCPLoginScreenPresenter_Host: self)
        GIDSignIn.sharedInstance().uiDelegate = self
    }
    
    func loggedIn(user: GIDGoogleUser)
    {
        if user.authentication != nil {
            presenter?.runGoogleLoginWithNSString(user.authentication.idToken, withNSString: user.profile.name, withNSString: nil, withNSString: nil)
        }
    }
    
    @objc func onLoginReturnedWithBoolean(failed: jboolean, withBoolean firstLogin: jboolean)
    {
        if(!failed)
        {
            performSegueWithIdentifier("Schedule", sender: self)
        }
    }
    
}
