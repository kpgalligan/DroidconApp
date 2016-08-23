//
//  Welcome4ViewController.swift
//  ios
//
//  Created by Ramona Harrison on 8/10/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit

class Welcome4ViewController: UIViewController {

    @IBOutlet weak var arrow: UIImageView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        arrow.image = arrow.image!.imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate)
        arrow.tintColor = UIColor(red: 93/255.0, green: 253/255.0, blue: 173/255.0, alpha: 1.0)
    }

}
