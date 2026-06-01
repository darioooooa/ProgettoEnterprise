import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InboxOrganizzatore } from './inbox-organizzatore';

describe('InboxOrganizzatore', () => {
  let component: InboxOrganizzatore;
  let fixture: ComponentFixture<InboxOrganizzatore>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InboxOrganizzatore],
    }).compileComponents();

    fixture = TestBed.createComponent(InboxOrganizzatore);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
