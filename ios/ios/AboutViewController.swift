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
    @IBOutlet weak var stackView: UIStackView!
    
    //var scrollView: UIScrollView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        navBar.translucent = false
        
//        scrollView = UIScrollView(frame: view.bounds)
//        scrollView.contentSize = stackView.bounds.size
//        scrollView.autoresizingMask = [.FlexibleWidth, .FlexibleHeight]
//        
//        scrollView.addSubview(stackView)
//        view.addSubview(scrollView)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

}
