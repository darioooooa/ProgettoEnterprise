import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InboxViaggiatore } from './inbox-viaggiatore';

describe('InboxViaggiatore', () => {
  let component: InboxViaggiatore;
  let fixture: ComponentFixture<InboxViaggiatore>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InboxViaggiatore],
    }).compileComponents();

    fixture = TestBed.createComponent(InboxViaggiatore);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
