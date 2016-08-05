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
    @IBOutlet weak var liveStreamButton: UIButton!
    
    var event: DCDEvent!
    var eventDetailPresenter: DCPEventDetailPresenter!
    
    func loadInfo(title: String, description: String, track: String, time: String, event: DCDEvent, eventDetailPresenter: DCPEventDetailPresenter) {
        self.event = event
        self.eventDetailPresenter = eventDetailPresenter
        titleLabel.text = title
        descriptionLabel.attributedText = formatHTMLString(description)
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

    func formatHTMLString(htmlString: String) -> NSAttributedString {
        let modifiedFont = NSString(format:"<span style=\"font: -apple-system-body; font-size: 12px\">%@</span>", htmlString) as String
        
        let attrStr = try! NSAttributedString(
            data: modifiedFont.dataUsingEncoding(NSUnicodeStringEncoding, allowLossyConversion: true)!,
            options: [NSDocumentTypeDocumentAttribute: NSHTMLTextDocumentType, NSCharacterEncodingDocumentAttribute: NSUTF8StringEncoding],
            documentAttributes: nil)
        
        return attrStr
    }

}
