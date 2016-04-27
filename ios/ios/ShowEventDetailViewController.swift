//
//  ShowEventDetailViewController.swift
//  ios
//
//  Created by Sahil Ishar on 3/14/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit

@objc class ShowEventDetailViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, DCPEventDetailHost {
    
    var titleString: String?
    var descriptionString: String?
    var dateTime: String?
    var trackNumString: String?
    var event: DCDEvent!
    var speakers: [DCDEventSpeaker]?
    var eventDetailPresenter: DCPEventDetailPresenter!
    
    @IBOutlet weak var tableView : UITableView!
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 0 {
            return 1
        }
        
        return speakers!.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            let cell:EventTableViewCell = tableView.dequeueReusableCellWithIdentifier("eventCell") as! EventTableViewCell
            
            cell.loadInfo(titleString!, description: descriptionString!, track: trackNumString!, time: dateTime!, event: event, eventDetailPresenter: eventDetailPresenter)
            cell.selectionStyle = UITableViewCellSelectionStyle.None
            return cell
        } else {
            let cell:SpeakerTableViewCell = tableView.dequeueReusableCellWithIdentifier("speakerCell") as! SpeakerTableViewCell
            
            let speaker = speakers![indexPath.row] as DCDEventSpeaker
            if let speakerDescription = speakers?[indexPath.row].valueForKey("userAccount_")!.valueForKey("profile_") {
                cell.loadInfo(speaker.valueForKey("userAccount_")!.valueForKey("name_") as! String, info: speakerDescription as! String)
            }
            cell.selectionStyle = UITableViewCellSelectionStyle.None
            return cell
        }
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if section == 0 {
            return nil
        }
        
        if speakers?.count > 1 {
            return "Speakers : "
        }
        
        return "Speaker : "
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == 0 {
            return 0.1
        }
        
        return 25
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 {
            let attrTitle = [NSFontAttributeName: UIFont.systemFontOfSize(16.0)]
            let szTitle = CGSize(width: view.bounds.width - 16, height:200)
            let rTitle = titleString!.boundingRectWithSize(szTitle, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes:attrTitle, context:nil)
            let htTitle = ceil(rTitle.size.height)
            
            let attrDescription = [NSFontAttributeName: UIFont.systemFontOfSize(14.0)]
            let szDescription = CGSize(width: view.bounds.width - 16, height:500)
            let rDescription = descriptionString!.boundingRectWithSize(szDescription, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes:attrDescription, context:nil)
            let htDescription = ceil(rDescription.size.height)

            return htTitle + htDescription + 50.0
        }

        
        if let speakerDescription = speakers?[indexPath.row].valueForKey("userAccount_")!.valueForKey("profile_") {
            let attrDescription = [NSFontAttributeName: UIFont.systemFontOfSize(14.0)]
            let szDescription = CGSize(width: view.bounds.width - 16, height:500)
            let rDescription = speakerDescription.boundingRectWithSize(szDescription, options: NSStringDrawingOptions.UsesLineFragmentOrigin, attributes:attrDescription, context:nil)
            let htDescription = ceil(rDescription.size.height)
            
            return htDescription + 133.0
        }
        
        return 200
    }
    
    func tableView(tableView: UITableView, estimatedHeightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return 500.0
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(true)
        
        let tintColor = UIColor(red:(125/255.0), green:(216/255.0), blue:(20/255.0), alpha: 1.0)
        navigationController!.navigationBar.barTintColor = tintColor
        UINavigationBar.appearance().tintColor = UIColor.whiteColor()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        eventDetailPresenter = DCPEventDetailPresenter(androidContentContext: DCPAppManager.getContext(), withLong: event.getId(), withDCPEventDetailHost: self)
        
        let nib = UINib(nibName: "EventTableViewCell", bundle: nil)
        tableView.registerNib(nib, forCellReuseIdentifier: "eventCell")
        
        let nib2 = UINib(nibName: "SpeakerTableViewCell", bundle: nil)
        tableView.registerNib(nib2, forCellReuseIdentifier: "speakerCell")
        
        self.tableView.contentInset = UIEdgeInsetsMake(1.0, 0.0, 0.0, 0.0);
        self.tableView.tableHeaderView = UIView(frame: CGRectMake(0.0, 0.0, tableView.bounds.size.width, 0.1))
        
        print(dateTime!)
        print(trackNumString!)
        
        self.tableView.reloadData()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func dataRefresh() {
        event = eventDetailPresenter.getEventDetailLoadTask().getEvent();
        speakers = PlatformContext_iOS.javaListToNSArray(eventDetailPresenter.getEventDetailLoadTask().getEvent().getSpeakerList()) as! [DCDEventSpeaker];
        tableView.reloadData();
    }
}
