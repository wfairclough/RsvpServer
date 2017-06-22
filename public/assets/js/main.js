var nav = new Vue({
  el: '#main-navigation',
  methods: {
    onNav: function() {
      $('.navbar-toggle').click()
    }
  }
})

var app5 = new Vue({
  el: '#welcome',
  data: {
    message: 'Katrina & Will'
  },
  methods: {
    reverseMessage: function () {
      this.message = this.message.split('').reverse().join('')
    }
  }
});

var wellWishesVm = new Vue({
  el: '#well-wishes',
  data: {
    
  },
  methods: {
    
  }
});

var giftsVm = new Vue({
  el: '#gifts',
  data: {
    
  },
  methods: {
    
  }
});

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

var GlobalID = 0;

function nextId() {
  return GlobalID++;
}

/*** Admin add Invitation ***/

var addInviteSec = new Vue({
  el: "#section-add-invite",
  data: {
    isAdmin: getParameterByName('admin') === 'true',
    isFetching: false,
    invitation: {
      code: '',
      guests: []
    },
    error: {
      data: {
        message: ''
      }
    }
  },
  mounted: function() {
    this.addNewGuest();
  },
  methods: {
    addNewGuest: function() {
      this.invitation.guests.push(this.generateGuest());
    },
    generateGuest: function() {
      return {
        id: nextId(),
        firstname: '',
        lastname: '',
        email: null,
        plusOne: false
      };
    },
    removeGuest: function(guest) {
      this.invitation.guests = this.invitation.guests.filter(function(g) {
        return g.id != guest.id;
      });
    },
    submitNewInvite: function() {
      var vm = this;
      vm.isFetching = true;
      axios.post('/api/invitations/create', this.invitation)
        .then(function(rsp) {
          console.log(rsp);
          vm.isFetching = false;
          vm.resetInvitation();
        })
        .catch(function(err) {
          console.error(err);
          vm.isFetching = false;
          vm.error = err;
        });
    },
    resetInvitation: function() {
      this.invitation = {
        code: '',
        guests: []
      };
      this.error = {
        data: {
          message: ''
        }
      }
      this.addNewGuest();
    },
    isInviteInvalid: function() {
      return !(this.invitation.guests.reduce(function(acc, g) {
        return acc && (g.firstname.length > 0 && g.lastname.length > 0);
      }, true) && this.invitation.code.length > 0);
    }
  }
});

var newGuestVm = Vue.component('new-guest', {
  props: ['guest', 'index'],
  data: function() {
    return {
      
    }
  },
  methods: {
    removeGuest: function() {
      this.$emit('remove-guest', this.guest);
    }
  },
  template: `
    <div class="row add-guest">
        
        <form class="col-xs-12 col-sm-12 col-md-6 col-md-offset-3">
            <div class="remove-guest">
              <button v-on:click="removeGuest" class="btn btn-danger" style="float:right;">Remove</button>
            </div>

            <label for="guestFirstName">First Name</label>
            <input type="text" v-model="guest.firstname" class="form-control" name="guestFirstName" placeholder="required" id="guestFirstName" required data-validation-required-message="Please enter the first name for guest" />

            <label for="guestLastName">Last Name</label>
            <input type="text" v-model="guest.lastname" class="form-control" name="guestLastName" placeholder="required" id="guestLastName" required data-validation-required-message="Please enter the last name for guest" />

            <label for="guestEmail">Email</label>
            <input type="text" v-model="guest.email" class="form-control" name="guestEmail" placeholder="optional" id="guestEmail" data-validation-required-message="Please enter the guests email" />
            
            <div class="checkbox">
            <label>
              <input type="checkbox" v-model="guest.plusOne" class="form-control" name="guestPlusOne" id="guestPlusOne" required /> Does this guest have a plus one?
            </label>
            </div>
        </form>
        
    </div>
  `
})

/*** End Admin Add Invite ***/

/*** RSVP Form ***/

const RsvpStage = {
  CODE: 'code',
  INVITE:  'invite',
  FINISHED:  'finished'
};

var rvspForm = new Vue({
  el: "#rsvpContainer",
  data: {
    message: 'RSVP',
    rsvpCode: '',
    invitation: {},
    stage: RsvpStage.CODE,
    isFetching: false,
    showRsvpHelp: false,
    menu: {
      items: []
    },
    rsvpForm: {
      notes: '',
      songChoice: ''
    }
  },
  created: function() {
    var vm = this;
    axios.get('/api/menu/items')
      .then(function(rsp) {
        console.log(rsp);
        vm.menu.items = rsp.data;
      })
      .catch(function(err) {
        console.error(err);
      });
  },
  methods: {
    submitCode: function() {
      var vm = this;
      this.isFetching = true;
      console.log(this.rsvpCode);
      axios.get('/api/invitations/'+this.rsvpCode)
        .then(function(rsp) {
          console.log(rsp);
          vm.isFetching = false;
          vm.stage = RsvpStage.INVITE;
          vm.invitation = rsp.data;
        })
        .catch(function(err) {
          console.error(err);
          vm.isFetching = false;
        });
    },
    submitRsvp: function() {
      var vm = this;
      this.isFetching = true;
      axios.put('/api/invitations/'+this.invitation.code+'/submit', {
        song: this.invitation.songRequest,
        notes: this.invitation.notes
      })
        .then(function(rsp) {
          console.log(rsp);
          vm.message = 'We look forward to seeing you!';
          vm.isFetching = false;
          vm.stage = RsvpStage.FINISHED;
          vm.invitation = rsp.data;
        })
        .catch(function(err) {
          console.error(err);
          vm.isFetching = false;
        });
    },
    updateInvitation: function(inv) {
      console.log('Update Invitation');
      // Calculate if any of the guests in the new invitation were just added
      var oldGuestKeys = this.invitation.guests.map(function(g) { return g.key });
      var updatedGuests = inv.guests.map(function(g) {
        g.justAdded = oldGuestKeys.indexOf(g.key) === -1;
        return g;
      });
      this.invitation.guests = updatedGuests;
    }
  }
});

Vue.component('guest', {
  // declare the props
  props: ['invitation', 'guest', 'index', 'menu'],
  data: function() {
    return {
      isAddingGuest: false,
      applied: false,
      guestOf: null,
      selectedMenuItem: null,
      plusOne: null
    }
  },
  created: function() {
    if (this.guest.justAdded) {
      this.toggleAppliedCover();
    }
    this.plusOne = this.getAddedGuest();
  },
  updated: function() {
    this.plusOne = this.getAddedGuest();
  },
  watch: {
    selectedMenuItem: function(newMenuItem) {
      console.log('Selected new menu Item: ' + newMenuItem);
      var vm = this;
      axios.put('/api/invitations/'+this.invitation.code+'/guests/'+this.guest.key+'/menu', 
        {
          menuItemKey: newMenuItem,
          notes: ''
        })
        .then(function(rsp) {
          console.log(rsp);
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
    }
  },
  methods: {
    toggleAppliedCover: function() {
      var vm = this;
      vm.applied = true;
      setTimeout(function() {
        vm.applied = false;
      }, 1000);
    },
    rsvp: function() {
      console.log('rsvp');
      var vm = this;
      axios.put('/api/invitations/'+this.invitation.code+'/guests/'+this.guest.key+'/rsvp', {rsvp: true})
        .then(function(rsp) {
          console.log(rsp);
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
    },
    unrsvp: function() {
      console.log('unrsvp');
      var vm = this;
      vm.isAddingGuest = false;
      axios.put('/api/invitations/'+this.invitation.code+'/guests/'+this.guest.key+'/rsvp', {rsvp: false})
        .then(function(rsp) {
          console.log(rsp);
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
    },
    addPlusOne: function() {
      console.log('addPlusOne');
      this.isAddingGuest = true;
    },
    removePlusOne: function() {
      console.log('removePlusOne');
      var vm = this;
      vm.isAddingGuest = false;
      var invitedGuest = this.getInvitedGuest();
      if (invitedGuest !== null) {
        axios.delete('/api/invitations/'+this.invitation.code+'/guests/'+invitedGuest.key)
        .then(function(rsp) {
          console.log(rsp);
          vm.plusOne = null;
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
      } else {
        // Lets let the server know that this guest has selected 'No' to bringing a guest
        axios.put('/api/invitations/'+this.invitation.code+'/guests/'+this.guest.key+'/noplusone', {hasPlusOne: false})
        .then(function(rsp) {
          console.log(rsp);
          vm.plusOne = null;
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
      }
    },
    getAddedGuest: function() {
      var vm = this;
      return vm.guest.plusOne && vm.invitation.guests.reduce(function(acc, g) {
        if (g.plusOneGuestKey === vm.guest.key) {
          acc = g;
        }
        return acc;
      }, null);
    },
    guestAdded: function(data) {
      console.log('guest added: ');
      console.log(data.newGuest);
      this.toggleAppliedCover();
      this.$emit('update-invitation', data.invitation);
    },
    getInvitedGuest: function() {
      var vm = this;
      var invitedGuests = vm.invitation.guests.filter(function(g) {
        return g.plusOneGuestKey === vm.guest.key;
      });
      if (invitedGuests.length > 0) {
        return invitedGuests[0];
      }
      return null;
    },
    getGuestOf: function() {
      var vm = this;
      return vm.invitation.guests.filter(function(g) {
        return g.key === vm.guest.plusOneGuestKey;
      })[0];
    },
    isSelectedMenuItem: function(item) {
      var selectedKey = this.guest.menuItem && this.guest.menuItem.menuItemKey;
      return selectedKey === item.key
    }
  },
  // just like data, the prop can be used inside templates
  // and is also made available in the vm as this.message
  template: `
  <div class="row">
    <div class="invitation-guest col-xs-12 col-sm-12 col-md-6 col-md-offset-3">
      <transition name="fade">
        <div class="updated-cover" v-if="applied"><i class="title fa fa-check-circle"></i></div>
      </transition>
      <h5 class="guest-name">{{guest.firstname + ' ' + guest.lastname}}<span v-if="guest.plusOneGuestKey !== undefined">(Guest of {{ getGuestOf().firstname }})</span></h5>
      <div class="question will-attend" v-if="guest.plusOneGuestKey === undefined">
          <p>Will {{guest.firstname}} be attending?</p>
          <div class="answers" v-bind:class="{ invalid: (guest.rsvp === undefined) }">
            <a class="answer" v-on:click="rsvp" v-bind:class="{ selected: guest.rsvp === true }">Yes</a>
            <a class="answer" v-on:click="unrsvp" v-bind:class="{ selected: guest.rsvp === false }">No</a>
          </div>
      </div>
      
      <hr v-if="guest.plusOne && guest.rsvp"/>
      <div class="question plus-one" v-if="guest.plusOne && guest.rsvp">
          <p>Will {{guest.firstname}} be bringing a guest?</p>
          <div class="answers" v-bind:class="{ invalid: (guest.hasAddedPlusOne === undefined) }">
            <a class="answer" v-on:click="addPlusOne" v-bind:class="{ selected: (guest.hasAddedPlusOne === true || isAddingGuest) }">Yes</a>
            <a class="answer" v-on:click="removePlusOne" v-bind:class="{ selected: (guest.hasAddedPlusOne === false && !isAddingGuest)}">No</a>
            <p v-if="plusOne !== null">({{ plusOne.firstname + ' ' + plusOne.lastname }})</p>
            <add-guest  v-if="(isAddingGuest && !guest.hasAddedPlusOne)"
                        v-on:guest-added="guestAdded"
                        v-bind:invitationCode="invitation.code" 
                        v-bind:addedByGuest="guest" />
          </div>
      </div>

      <hr v-if="guest.rsvp && guest.plusOneGuestKey === undefined"/>
      <div class="question select-meal" v-if="guest.rsvp">
          <p>Please select {{guest.firstname}}'s choice entr&eacute;e?</p>
          <div class="menu" v-bind:class="{ invalid: (guest.menuItem === undefined) }">
            <div class="menu-item" 
              v-for="item in menu.items" 
              v-bind:class="{ selected: isSelectedMenuItem(item) }"
              @click="selectedMenuItem = item.key">
                <a class="answer">{{ item.name }}</a>
                <p>{{ item.description }}</p>
            </div>
          </div>
      </div>
    </div>
  </div>
  `
});

Vue.component('add-guest', {
  // declare the props
  props: ['invitationCode', 'addedByGuest'],
  data: function() {
    return {
      firstname: '',
      lastname: '',
    }
  },
  methods: {
    addGuest: function() {
      var vm = this;
      var guest = {
        firstname: this.firstname,
        lastname: this.lastname,
        guestOf: this.addedByGuest,
        invitationCode: this.invitationCode
      };
      axios.post('/api/invitations/'+this.invitationCode+'/guests/'+this.addedByGuest.key+'/add', guest)
        .then(function(rsp) {
          console.log(rsp);
          vm.$emit('guest-added', {
            newGuest: guest,
            invitation: rsp.data
          })
        })
        .catch(function(err) {
          console.error(err);
        });
    }
  },
  template: `
  <div class="row">
    <div class="col-sm-10 col-sm-offset-1">
      <div class="row add-guest">
        <form class="add-guest-form" v-on:submit.prevent="addGuest">
          <input type="text" v-model="firstname" class="form-control col-md-8 col-md-offset-1" name="firstname" placeholder="First Name" required data-validation-required-message="Please enter the guests first name" />
          <input type="text" v-model="lastname" class="form-control col-md-8 col-md-offset-1" name="lastname" placeholder="Last Name" required data-validation-required-message="Please enter the guests first name" />
          <input class="btn btn-primary mt5 add-guest-submit" type="submit" name="addGuest" value="Add Guest" />
        </form>
      </div>
    </div>
  </div>
  `
});
/*** RSVP Form End ***/

