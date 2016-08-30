//
//  ShowEventDetailViewController.swift
//  ios
//
//  Created by Sahil Ishar on 3/14/16.
//  Copyright © 2016 Kevin Galligan. All rights reserved.
//

import UIKit

@objc class ShowEventDetailViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, DCPEventDetailHost {
    
    // MARK: Properties
    
    var titleString: String?
    var descriptionString: String?
    var dateTime: String?
    var trackNumString: String?
    var event: DCDEvent!
    var speakers: [DCDEventSpeaker]?
    var eventDetailPresenter: DCPEventDetailPresenter!
    
    @IBOutlet weak var tableView : UITableView!
    @IBOutlet weak var rsvpButton: UIButton!
    
    // MARK: Lifecycle events
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(true)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if eventDetailPresenter != nil {
            eventDetailPresenter.unregister()
        }
        
        eventDetailPresenter = DCPEventDetailPresenter(androidContentContext: DCPAppManager.getContext(), withLong: event.getId(), withDCPEventDetailHost: self)
        
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 800

        let nib = UINib(nibName: "EventTableViewCell", bundle: nil)
        tableView.registerNib(nib, forCellReuseIdentifier: "eventCell")
        
        let nib2 = UINib(nibName: "SpeakerTableViewCell", bundle: nil)
        tableView.registerNib(nib2, forCellReuseIdentifier: "speakerCell")
        
        self.tableView.contentInset = UIEdgeInsetsZero
        self.tableView.separatorStyle = .None
        self.tableView.reloadData()
        
        styleButton()
    }

    override func viewDidDisappear(animated: Bool) {
        eventDetailPresenter.unregister()
    }
    
    // MARK: Data refresh
    
    func dataRefresh() {
        event = eventDetailPresenter.getEventDetailLoadTask().getEvent()
        speakers = PlatformContext_iOS.javaListToNSArray(eventDetailPresenter.getEventDetailLoadTask().getEvent().getSpeakerList()) as? [DCDEventSpeaker]
        tableView.reloadData()
        updateButton()
    }
    
    func callStreamActivityWithDCTStartWatchVideoTask(task: DCTStartWatchVideoTask){
        performSegueWithIdentifier("LiveStream", sender: self)
    }
    
    func reportErrorWithNSString(error: String){
        print(error)
//        let alert = UIAlertView(title: "Video Error", message: error as String, delegate: nil, cancelButtonTitle: "Ok")
//        alert.show()
    }
    
    func resetStreamProgress() {
        // TODO
    }
    
    func showTicketOptionsWithNSString(email: String!, withNSString link: String!, withNSString cover: String!) {
        // TODO
    }

    
    // MARK: TableView
    
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
            if (event.isNow() && event.getStreamUrl() != nil) {
                cell.liveStreamButton.addTarget(self, action: #selector(ShowEventDetailViewController.liveStreamTapped(_:)), forControlEvents: UIControlEvents.TouchUpInside)
            } else {
                cell.liveStreamButton.hidden = true
                cell.liveStreamIcon.hidden = true
            }
            return cell
        } else {
            let cell:SpeakerTableViewCell = tableView.dequeueReusableCellWithIdentifier("speakerCell") as! SpeakerTableViewCell
            
            let speaker = speakers![indexPath.row] as DCDEventSpeaker
            if let speakerDescription = speakers?[indexPath.row].valueForKey("userAccount_")!.valueForKey("profile_") {
                let userAccount = speaker.getUserAccount()
                let imageUrl = userAccount!.avatarImageUrl() ?? ""
                cell.loadInfo(userAccount!.valueForKey("name_") as! String, info: speakerDescription as! String, imgUrl: imageUrl)
            }
            cell.selectionStyle = UITableViewCellSelectionStyle.None
            return cell
        }
    }

    // MARK: Action
    
    func styleButton() {
        rsvpButton.layer.cornerRadius = 24
        rsvpButton.layer.masksToBounds = true
        rsvpButton.layer.shadowColor = UIColor.blackColor().CGColor
        rsvpButton.layer.shadowOffset = CGSizeMake(5, 5)
        rsvpButton.layer.shadowRadius = 5
        rsvpButton.layer.shadowOpacity = 1.0
        updateButton()
    }

    
    func updateButton() {
        if (event.isRsvped()) {
            rsvpButton.setImage(UIImage(named: "ic_done"), forState: .Normal)
            rsvpButton.backgroundColor = UIColor.whiteColor()
        } else {
            rsvpButton.setImage(UIImage(named: "ic_add"), forState: .Normal)
            rsvpButton.backgroundColor = UIColor(red: 93/255.0, green: 253/255.0, blue: 173/255.0, alpha: 1.0)
        }
    }
    
    @IBAction func toggleRsvp(sender: UIButton) {
        eventDetailPresenter.toggleRsvp()
    }
    
    func liveStreamTapped(sender: UIButton) {
        eventDetailPresenter.callStartVideoWithNSString(event.getStreamUrl(), withNSString: event.getCoverUrl())
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if(segue.identifier == "LiveStream") {
            let liveStreamVC = (segue.destinationViewController as! LiveStreamViewController)
            liveStreamVC.titleString = titleString
            liveStreamVC.streamUrl = event.getStreamUrl()
            liveStreamVC.coverUrl = event.getCoverUrl()
        }
    }
}
