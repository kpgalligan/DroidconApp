//
//  EventTableViewCell.swift
//  ios
//
//  Created by Sahil Ishar on 3/15/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit

class EventTableViewCell: UITableViewCell {

    @IBOutlet weak var titleLabel : UILabel!
    @IBOutlet weak var descriptionLabel : UILabel!
    @IBOutlet weak var timeInfoLabel : UILabel!
    
    
    func loadInfo(title: String, description: String, track: String, time: String) {
        titleLabel.text = title
        descriptionLabel.text = description
        timeInfoLabel.text = "Track " + track + ", " + time
        
        titleLabel.sizeToFit()
        descriptionLabel.sizeToFit()
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
